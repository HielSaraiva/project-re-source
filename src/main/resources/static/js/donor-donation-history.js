(() => {
    const timeline = document.querySelector('[data-donor-history]');
    if (!timeline || timeline.dataset.historyFlow === 'completed') return;
    const dates = window.resourceDonationDeadlines;
    const donor = timeline.dataset.donorName;
    const append = (title, at, actor, description) => {
        if (!at || !Number.isFinite(Date.parse(at))) return;
        const item = document.createElement('li');
        item.dataset.sessionEvent = '';
        const dateLabel = /^\d{4}-\d{2}-\d{2}$/.test(at) ? at.split('-').reverse().join('/') : dates.dateTime(at);
        for (const [tag, value] of [['strong', title], ['time', dateLabel], ['span', actor], ['p', description]]) {
            const element = document.createElement(tag);
            element.textContent = value;
            if (tag === 'time') element.dateTime = at;
            item.append(element);
        }
        timeline.append(item);
    };
    const updateDate = (item, at) => {
        const time = item?.querySelector('time');
        if (!time) return;
        time.dateTime = at;
        time.textContent = dates.dateTime(at);
    };
    const render = () => {
        timeline.querySelectorAll('[data-session-event]').forEach(item => item.remove());
        if (timeline.dataset.historyFlow === 'proposal') {
            if (document.querySelector('[data-proposal-rejected]')?.dataset.proposalRejected === 'true') return;
            const deadline = dates.stage('acceptance');
            if (deadline) updateDate(timeline.firstElementChild, dates.startedAt(deadline));
            const state = window.resourceProposalState.read();
            if (state?.cancelled) append('Proposta cancelada', state.cancelledAt,
                state.reason === 'acceptance_expired' ? 'Sistema' : donor,
                state.reason === 'acceptance_expired' ? 'Prazo de aceite encerrado sem resposta da ONG. Itens e reserva da necessidade liberados.'
                    : 'Cancelamento solicitado pelo doador. Itens e reserva da necessidade liberados.');
            return;
        }
        const page = timeline.closest('main');
        const choiceDeadline = page.dataset.expiredStage ? page.dataset.choiceDeadline : dates.stage('choice');
        if (choiceDeadline) {
            const acceptedAt = dates.startedAt(choiceDeadline);
            updateDate(timeline.children[0], new Date(Date.parse(acceptedAt) - 86400000).toISOString());
            updateDate(timeline.children[1], acceptedAt);
        }
        const state = window.resourceDeliveryState.read();
        if (state?.method) append(state.method === 'carrier' ? 'Envio pelos Correios escolhido' : 'Entrega presencial escolhida',
            state.methodConfirmedAt, donor, 'Modalidade confirmada. Iniciado o prazo de 7 dias para confirmar a entrega ou a postagem.');
        if (state?.status === 'shipped' || state?.status === 'awaiting_ngo_confirmation') {
            const carrier = state.method === 'carrier';
            append(carrier ? 'Postagem informada' : 'Entrega presencial informada', state.reportedAt || state.date, donor,
                (carrier ? 'Postagem em ' + state.date.split('-').reverse().join('/') + '. Rastreamento: ' + state.trackingCode + '.'
                    : 'Entrega em ' + state.date.split('-').reverse().join('/') + '. Recebido por ' + state.recipient + '.')
                + (state.notes ? ' Observações: ' + state.notes : '')
                + ' Aguardando confirmação de recebimento pela ONG.');
        }
        if (state?.status === 'cancelled') {
            const expired = state.reason === 'deadline_expired';
            const stages = { choice: 'escolher a modalidade', in_person: 'confirmar a entrega presencial', carrier: 'confirmar a postagem' };
            append('Doação cancelada', state.cancelledAt, expired ? 'Sistema' : donor,
                (expired ? 'Prazo de 7 dias para ' + stages[state.expiryStage] + ' encerrado.' : 'Cancelamento solicitado pelo doador.')
                + ' Itens e reserva da necessidade liberados.');
        }
    };
    render();
    window.addEventListener('resource:donation-updated', render);
    window.addEventListener('pageshow', render);
    window.addEventListener('focus', render);
    setInterval(render, 30000);
})();
