(() => {
    document.querySelectorAll('[data-rejected-protocol]').forEach(row =>
        window.resourceDonationReservations.release(row.dataset.rejectedProtocol, document.querySelector('[data-rejected-at]').dataset.rejectedAt));
    document.querySelectorAll('[data-cancelled-protocol]').forEach(row =>
        window.resourceDonationReservations.release(row.dataset.cancelledProtocol, document.querySelector('[data-cancelled-at]').dataset.cancelledAt));
    const row = document.querySelector('[data-delivery-shipment="monitor-instituto"]');
    const pending = document.querySelector('[data-pending-proposal="monitor-associacao"]');
    const updateProposal = () => {
        if (!pending) return;
        const deadline = window.resourceDonationDeadlines.stage('acceptance');
        if (deadline) pending.querySelector('[data-shipment-deadline]').textContent = 'Aceite até ' + window.resourceDonationDeadlines.dateTime(deadline);
        if (!window.resourceProposalState.read()?.cancelled) return;
        const badge = pending.querySelector('.status-badge');
        badge.textContent = 'Doação cancelada';
        pending.querySelector('[data-shipment-deadline]').hidden = true;
        badge.classList.remove('status--awaiting-acceptance');
        badge.classList.add('status--cancelled');
    };
    updateProposal();
    window.addEventListener('pageshow', updateProposal);
    if (!row) return;
    const update = () => {
        const state = window.resourceDeliveryState.read();
        const badge = row.querySelector('.status-badge');
        const link = row.querySelector('.text-link');
        const deadline = row.querySelector('[data-shipment-deadline]');
        deadline.hidden = ['shipped', 'cancelled', 'awaiting_ngo_confirmation'].includes(state?.status);
        const due = window.resourceDeliveryState.deadline();
        deadline.textContent = (state?.method === 'in_person' ? 'Entrega até ' : state?.method === 'carrier' ? 'Postagem até ' : 'Escolha até ') + window.resourceDonationDeadlines.dateTime(due);
        badge.classList.remove('status--awaiting-shipment', 'status--shipped', 'status--cancelled');
        badge.classList.add(state?.status === 'shipped' ? 'status--shipped' : state?.status === 'cancelled' ? 'status--cancelled' : 'status--awaiting-shipment');
        badge.textContent = state?.status === 'shipped' ? 'Enviado' : state?.status === 'cancelled' ? 'Doação cancelada'
            : state?.status === 'awaiting_ngo_confirmation' ? 'Aguardando confirmação da ONG'
            : state?.method === 'in_person' ? 'Aguardando entrega' : 'Aguardando envio';
        link.textContent = state?.method || state?.status === 'cancelled' ? 'Ver detalhes' : 'Opções de envio';
    };
    update();
    window.addEventListener('pageshow', update);
    window.addEventListener('focus', update);
    setInterval(update, 30000);
})();
