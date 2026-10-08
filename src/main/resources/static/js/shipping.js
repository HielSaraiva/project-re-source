(() => {
    const dialog = document.querySelector('#delivery-method-confirmation');
    if (!dialog || typeof dialog.showModal !== 'function') return;
    const buttons = [...document.querySelectorAll('[data-delivery-method] .action-button')];
    const confirm = dialog.querySelector('[data-confirm-method]');
    const cancellation = document.querySelector('#shipping-cancellation');
    const cancelButton = document.querySelector('[data-open-cancellation]');
    const confirmCancellation = cancellation.querySelector('[data-confirm-cancellation]');
    let choice = null;
    let trigger = null;
    const locked = (state) => ['cancelled', 'shipped', 'awaiting_ngo_confirmation'].includes(state?.status);
    const updateChoice = () => {
        const state = window.resourceDeliveryState.read();
        const cancelled = state?.status === 'cancelled';
        const dates = window.resourceDonationDeadlines;
        const deadline = window.resourceDeliveryState.deadline();
        const banner = document.querySelector('[data-active-deadline]');
        banner.querySelector('strong').textContent = state?.method === 'in_person' ? 'Prazo para confirmar a entrega presencial'
            : state?.method === 'carrier' ? 'Prazo para confirmar a postagem' : 'Prazo para escolher a modalidade de entrega';
        dates.render(banner, deadline, state?.method ? 'Modalidade escolhida em ' + dates.date(state.methodConfirmedAt)
            : 'Aceite da ONG em ' + dates.date(dates.startedAt(deadline)), state?.method ? '7 dias após a escolha' : '7 dias após o aceite');
        buttons.forEach(button => button.setAttribute('aria-disabled', String(!state?.method && dates.expired(deadline))));
        document.querySelector('[data-active-deadline]').hidden = locked(state);
        const status = document.querySelector('[data-shipping-status]');
        status.classList.toggle('shipping-status--cancelled', cancelled);
        status.classList.toggle('delivery-status--shipped', state?.status === 'shipped');
        status.textContent = cancelled ? 'Doação cancelada' : state?.status === 'shipped' ? 'Enviado'
            : state?.status === 'awaiting_ngo_confirmation' ? 'Aguardando confirmação da ONG'
            : state?.method === 'in_person' ? 'Aguardando entrega' : 'Aguardando envio';
        document.querySelector('.options-cards').hidden = cancelled;
        document.querySelector('#shipping-cancellation-actions').hidden = locked(state);
        const notice = document.querySelector('#delivery-method-notice');
        notice.classList.toggle('delivery-method-notice--cancelled', cancelled);
        if (cancelled) {
            if (dialog.open) dialog.close();
            if (cancellation.open) cancellation.close();
            notice.hidden = true;
            document.querySelector('.page-heading h1').textContent = 'Doação cancelada';
            const automatic = state.reason === 'deadline_expired';
            document.querySelector('#delivery-cancelled').hidden = false;
            const stages = { choice: 'Escolha da modalidade de entrega', in_person: 'Confirmação da entrega presencial', carrier: 'Confirmação da postagem' };
            document.querySelector('[data-cancellation-explanation]').textContent = automatic
                ? 'O prazo de 7 dias terminou sem a conclusão desta etapa. A doação foi cancelada automaticamente e não pode mais ser enviada ou entregue.'
                : 'O envio foi cancelado a seu pedido. O cancelamento não pode ser desfeito.';
            document.querySelector('[data-expired-stage-row]').hidden = !automatic;
            document.querySelector('[data-expired-deadline-row]').hidden = !automatic;
            document.querySelector('[data-expired-stage-label]').textContent = stages[state.expiryStage] || '';
            const setDate = selector => {
                const time = document.querySelector(selector);
                time.textContent = state.cancelledAt ? dates.dateTime(state.cancelledAt) : 'Data não registrada';
                if (state.cancelledAt) time.dateTime = state.cancelledAt;
            };
            setDate('[data-cancellation-date]');
            setDate('[data-expired-deadline]');
            document.querySelector('[data-shipping-description]').textContent = 'Você pode voltar ao painel para acompanhar suas outras doações.';
            return;
        }
        if (!state?.method) return;
        document.querySelectorAll('[data-delivery-method]').forEach(card => { card.hidden = card.dataset.deliveryMethod !== state.method; });
        notice.hidden = false;
        notice.textContent = state.status === 'shipped' ? 'Postagem registrada. Aguarde a entrega do pacote e a confirmação de recebimento pela ONG.'
            : state.status === 'awaiting_ngo_confirmation' ? 'Entrega presencial informada. A doação está Aguardando confirmação da ONG.'
            : state.method === 'carrier' ? 'Envio pelos Correios confirmado. Informe o rastreamento após a postagem. A modalidade não pode mais ser alterada.'
            : 'Entrega presencial confirmada. A doação está Aguardando entrega e a modalidade não pode mais ser alterada.';
        const selected = buttons.find(button => button.closest('[data-delivery-method]').dataset.deliveryMethod === state.method);
        selected.textContent = state.method === 'carrier'
            ? state.status === 'shipped' ? 'Ver detalhes do envio' : 'Informar postagem'
            : 'Acompanhar entrega presencial';
    };
    updateChoice();
    window.addEventListener('pageshow', updateChoice);
    window.addEventListener('focus', updateChoice);
    setInterval(updateChoice, 30000);
    buttons.forEach(button => button.addEventListener('click', event => {
        const state = window.resourceDeliveryState.read();
        const method = button.closest('[data-delivery-method]').dataset.deliveryMethod;
        if (state?.status === 'cancelled' || (state?.method && state.method !== method)) {
            event.preventDefault();
            updateChoice();
            return;
        }
        if (state?.method === method) return;
        if (window.resourceDonationDeadlines.expired(window.resourceDeliveryState.deadline())) { event.preventDefault(); updateChoice(); return; }
        event.preventDefault();
        choice = method;
        trigger = button;
        const carrier = method === 'carrier';
        document.querySelector('#method-title').textContent = carrier ? 'Confirmar envio pelos Correios' : 'Confirmar entrega presencial';
        document.querySelector('[data-method-summary]').textContent = carrier ? 'Você escolheu enviar a doação pelos Correios para ' : 'Você escolheu levar a doação diretamente à ';
        document.querySelector('[data-method-status]').textContent = carrier ? 'Aguardando envio' : 'Aguardando entrega';
        confirm.textContent = carrier ? 'Confirmar envio pelos Correios' : 'Confirmar entrega presencial';
        confirm.disabled = false;
        dialog.showModal();
    }));
    dialog.querySelector('[data-cancel-method]').addEventListener('click', () => dialog.close());
    dialog.addEventListener('close', () => {
        if (!dialog.open) choice = null;
        trigger?.focus();
    });
    confirm.addEventListener('click', () => {
        if (!dialog.open || confirm.disabled || !choice || !trigger) return;
        const state = window.resourceDeliveryState.read();
        if ((!state?.method && window.resourceDonationDeadlines.expired(window.resourceDeliveryState.deadline())) || locked(state) || (state?.method && state.method !== choice)) {
            dialog.close();
            updateChoice();
            return;
        }
        confirm.disabled = true;
        if (!state?.method) window.resourceDeliveryState.write({ method: choice, status: choice === 'carrier' ? 'awaiting_shipping' : 'awaiting_delivery' });
        window.location.assign(trigger.href);
    });
    cancelButton.addEventListener('click', () => {
        if (locked(window.resourceDeliveryState.read())) return;
        confirmCancellation.disabled = false;
        cancellation.showModal();
    });
    cancellation.querySelector('[data-close-cancellation]').addEventListener('click', () => cancellation.close());
    cancellation.addEventListener('close', () => {
        if (window.resourceDeliveryState.read()?.status === 'cancelled') document.querySelector('.back-link').focus();
        else cancelButton.focus();
    });
    confirmCancellation.addEventListener('click', () => {
        if (!cancellation.open || confirmCancellation.disabled) return;
        if (locked(window.resourceDeliveryState.read())) {
            cancellation.close();
            updateChoice();
            return;
        }
        confirmCancellation.disabled = true;
        window.resourceDeliveryState.write({ status: 'cancelled' });
        updateChoice();
        cancellation.close();
    });
})();
