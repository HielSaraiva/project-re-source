(() => {
    const form = document.querySelector('#in-person-delivery-form');
    const review = document.querySelector('#delivery-review');
    const result = document.querySelector('#delivery-result');
    if (!form || !review || !result || typeof review.showModal !== 'function') return;

    const redirectIfCancelled = () => {
        const state = window.resourceDeliveryState.read();
        if (state?.method === 'carrier') {
            window.location.replace(document.querySelector('.personal-delivery').dataset.carrierUrl);
            return true;
        }
        if (state?.status !== 'cancelled') return false;
        window.location.replace(document.querySelector('.back-link').href);
        return true;
    };
    if (redirectIfCancelled()) return;

    const submit = form.querySelector('[type="submit"]');
    const recipient = form.elements.namedItem('recipient');
    const confirm = review.querySelector('[data-confirm-delivery]');
    let draft = null;
    let sent = false;

    const formattedDate = (value) => value.split('-').reverse().join('/');
    recipient.addEventListener('input', () => recipient.setCustomValidity(''));
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        if (!restore() || sent) return;
        recipient.setCustomValidity(recipient.value.trim() ? '' : 'Informe quem recebeu os itens.');
        if (!form.reportValidity()) return;
        draft = {
            date: form.elements.namedItem('date').value,
            recipient: recipient.value.trim(),
            notes: form.elements.namedItem('notes').value.trim()
        };
        review.querySelector('[data-review-date]').textContent = formattedDate(draft.date);
        review.querySelector('[data-review-recipient]').textContent = draft.recipient;
        review.querySelector('[data-review-notes]').textContent = draft.notes;
        review.querySelector('[data-review-notes-row]').hidden = !draft.notes;
        confirm.disabled = false;
        review.showModal();
    });

    // Local PoC state: delivery is only marked sent after explicit confirmation.
    confirm.addEventListener('click', () => {
        if (!restore()) return;
        if (!review.open || !draft || sent) return;
        sent = true;
        confirm.disabled = true;
        window.resourceDeliveryState.write({ ...draft, status: 'awaiting_ngo_confirmation', method: 'in_person' });
        restore();
        review.close();
        result.showModal();
    });
    review.querySelector('[data-close-review]').addEventListener('click', () => review.close());
    review.addEventListener('close', () => {
        draft = null;
        if (!sent) submit.focus();
    });
    result.querySelector('[data-close-result]').addEventListener('click', () => result.close());
    result.addEventListener('close', () => document.querySelector('#sent-title').focus());
    const restore = () => {
        if (redirectIfCancelled()) return false;
        const previous = window.resourceDeliveryState.read();
        if (previous?.method === 'in_person') {
            const back = document.querySelector('.back-link');
            back.href = document.querySelector('.brand').href;
            back.lastChild.textContent = ' Voltar ao painel';
        }
        if (previous?.status === 'awaiting_ngo_confirmation') {
            sent = true;
            document.querySelector('[data-active-deadline]').hidden = true;
            document.querySelector('[data-delivery-status]').textContent = 'Aguardando confirmação da ONG';
            document.querySelector('[data-sent-date]').textContent = formattedDate(previous.date);
            document.querySelector('[data-sent-recipient]').textContent = previous.recipient;
            document.querySelector('[data-sent-notes]').textContent = previous.notes;
            document.querySelector('[data-sent-notes-row]').hidden = !previous.notes;
            document.querySelector('#delivery-report').hidden = true;
            document.querySelector('#delivery-sent').hidden = false;
            form.querySelectorAll('input, textarea, button').forEach(control => { control.disabled = true; });
        } else {
            submit.disabled = false;
        }
        return sent || window.resourceDeliveryState.canConfirm(form);
    };
    restore();
    window.addEventListener('pageshow', restore);
})();
