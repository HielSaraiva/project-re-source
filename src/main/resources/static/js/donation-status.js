(() => {
    const button = document.querySelector('[data-open-cancellation]');
    const confirmation = document.querySelector('#donation-cancellation');
    const result = document.querySelector('#cancellation-result');
    if (!button || typeof confirmation?.showModal !== 'function' || !result) return;
    const confirm = confirmation.querySelector('[data-confirm-cancellation]');
    const update = () => {
        const state = window.resourceProposalState.read();
        if (!state?.cancelled) return;
        const cancelledAt = new Date(state.cancelledAt);
        const hasDate = !Number.isNaN(cancelledAt.getTime());
        document.querySelectorAll('[data-cancellation-date]').forEach(element => {
            element.textContent = hasDate
                ? cancelledAt.toLocaleDateString('pt-BR', { timeZone: 'America/Fortaleza' }) + ' às '
                    + cancelledAt.toLocaleTimeString('pt-BR', { timeZone: 'America/Fortaleza', hour: '2-digit', minute: '2-digit' })
                : 'Data não registrada';
            if (hasDate) element.dateTime = cancelledAt.toISOString();
            else element.removeAttribute('datetime');
        });
        document.querySelector('[data-donation-status]').textContent = 'Doação cancelada';
        document.querySelector('[data-donation-status]').classList.add('status-badge--cancelled');
        document.querySelector('[data-status-title]').textContent = 'Proposta cancelada';
        document.querySelector('[data-status-description]').textContent = state.reason === 'acceptance_expired'
            ? 'A ONG não aceitou a proposta no prazo de 7 dias. A doação foi cancelada e os itens voltaram a ficar disponíveis em Minhas doações.'
            : 'Esta proposta foi cancelada. A ONG não poderá mais aceitá-la. Consulte outras necessidades para fazer uma nova proposta.';
        document.querySelector('#pending-actions').hidden = true;
        document.querySelector('#cancelled-notice').hidden = false;
    };
    update();
    window.addEventListener('pageshow', update);
    button.addEventListener('click', () => {
        if (window.resourceProposalState.read()?.cancelled) return;
        confirm.disabled = false;
        confirmation.showModal();
    });
    confirmation.querySelector('[data-close-cancellation]').addEventListener('click', () => confirmation.close());
    confirmation.addEventListener('close', () => {
        if (!window.resourceProposalState.read()?.cancelled) button.focus();
    });
    confirm.addEventListener('click', () => {
        if (!confirmation.open || confirm.disabled) return;
        confirm.disabled = true;
        const cancelled = window.resourceProposalState.cancel();
        update();
        confirmation.close();
        if (cancelled) result.showModal();
    });
    result.querySelector('[data-close-result]').addEventListener('click', () => result.close());
    result.addEventListener('close', () => document.querySelector('#cancelled-title').focus());
})();
