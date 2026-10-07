(() => {
    const page = document.querySelector('.carrier-delivery');
    const form = document.querySelector('#carrier-delivery-form');
    const review = document.querySelector('#carrier-review');
    const result = document.querySelector('#carrier-result');
    if (!page || !form || typeof review?.showModal !== 'function') return;
    const submit = form.querySelector('[type="submit"]');
    const confirm = review.querySelector('[data-confirm-posting]');
    const tracking = form.elements.namedItem('trackingCode');
    let draft = null;
    let sent = false;
    const dateLabel = value => value.split('-').reverse().join('/');
    const restore = () => {
        const state = window.resourceDeliveryState.read();
        if (state?.method !== 'carrier') {
            window.location.replace(state?.method === 'in_person' ? page.dataset.inPersonUrl : page.dataset.shippingUrl);
            return false;
        }
        if (state.status === 'shipped') {
            sent = true;
            document.querySelector('[data-active-deadline]').hidden = true;
            const badge = document.querySelector('[data-carrier-status]');
            badge.textContent = 'Enviado';
            badge.classList.add('delivery-status--shipped');
            document.querySelector('#carrier-report').hidden = true;
            document.querySelector('#carrier-sent').hidden = false;
            document.querySelector('[data-sent-date]').textContent = dateLabel(state.date);
            document.querySelector('[data-sent-tracking]').textContent = state.trackingCode;
            document.querySelector('[data-sent-notes]').textContent = state.notes;
            document.querySelector('[data-sent-notes-row]').hidden = !state.notes;
            form.querySelectorAll('input, textarea, button').forEach(control => { control.disabled = true; });
        }
        return sent || window.resourceDeliveryState.canConfirm(form);
    };
    if (!restore()) return;
    window.addEventListener('pageshow', restore);
    tracking.addEventListener('input', () => { tracking.value = tracking.value.toUpperCase(); });
    form.addEventListener('submit', event => {
        event.preventDefault();
        if (!restore() || sent || !form.reportValidity()) return;
        draft = { date: form.elements.date.value, trackingCode: tracking.value.toUpperCase(), notes: form.elements.notes.value.trim() };
        review.querySelector('[data-review-date]').textContent = dateLabel(draft.date);
        review.querySelector('[data-review-tracking]').textContent = draft.trackingCode;
        review.querySelector('[data-review-notes]').textContent = draft.notes;
        review.querySelector('[data-review-notes-row]').hidden = !draft.notes;
        confirm.disabled = false;
        review.showModal();
    });
    review.querySelector('[data-close-review]').addEventListener('click', () => review.close());
    review.addEventListener('close', () => {
        draft = null;
        if (!sent) submit.focus();
    });
    confirm.addEventListener('click', () => {
        if (!review.open || !draft || confirm.disabled || !restore() || sent) return;
        confirm.disabled = true;
        window.resourceDeliveryState.write({ ...draft, method: 'carrier', status: 'shipped' });
        restore();
        review.close();
        result.showModal();
    });
    result.querySelector('[data-close-result]').addEventListener('click', () => result.close());
    result.addEventListener('close', () => document.querySelector('#sent-title').focus());
    submit.disabled = sent;
})();
