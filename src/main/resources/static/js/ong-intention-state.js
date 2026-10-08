(() => {
    const labels = {
        awaiting_acceptance: 'Aguardando aceite', awaiting_shipment: 'Aguardando envio',
        awaiting_delivery: 'Aguardando entrega', awaiting_ngo_confirmation: 'Aguardando confirmação da ONG',
        shipped: 'Enviado', completed: 'Concluído', rejected: 'Recusado', cancelled: 'Doação cancelada'
    };
    const storage = window.resourceMockStorage.create('resource:mock:ong:intentions', value => {
        if (!value || typeof value !== 'object' || Array.isArray(value)) return null;
        return Object.values(value).every(state => labels[state.status] && Array.isArray(state.events)
            && state.events.every(event => typeof event.title === 'string' && typeof event.actor === 'string'
                && typeof event.description === 'string' && Number.isFinite(Date.parse(event.at)))) ? value : null;
    });
    const date = value => {
        const instant = new Date(value);
        return instant.toLocaleDateString('pt-BR', { timeZone: 'America/Fortaleza' }) + ' às '
            + instant.toLocaleTimeString('pt-BR', {
                timeZone: 'America/Fortaleza', hour: '2-digit', minute: '2-digit', hourCycle: 'h23'
            });
    };
    const read = element => {
        const states = storage.read() || {};
        const id = element.dataset.intentionId;
        let state = states[id] || { status: element.dataset.status, events: [] };
        if (state.status === 'awaiting_acceptance' && Date.now() >= Date.parse(element.dataset.deadline)) {
            state = { status: 'cancelled', events: [...state.events, {
                title: 'Proposta cancelada', actor: 'Sistema', description: 'Prazo de aceite encerrado sem resposta da ONG.', at: element.dataset.deadline
            }] };
            storage.write({ ...states, [id]: state });
        }
        return state;
    };
    const decide = (element, action, reason) => {
        const current = read(element);
        if (!['accept', 'reject', 'receive'].includes(action)) return false;
        if (action === 'receive' ? !['awaiting_ngo_confirmation', 'shipped'].includes(current.status)
            : current.status !== 'awaiting_acceptance') return false;
        if (action === 'reject' && (!reason.trim() || reason.length > 1000)) return false;
        const accepted = action === 'accept';
        const received = action === 'receive';
        const event = {
            title: received ? 'Recebimento confirmado' : accepted ? 'Proposta aceita' : 'Proposta recusada', actor: 'ONG EcoVida',
            description: received ? 'Recebimento integral dos itens confirmado pela ONG. Doação concluída.'
                : accepted ? 'Doador tem 7 dias para escolher a modalidade de entrega.' : reason.trim(), at: new Date().toISOString()
        };
        storage.write({ ...(storage.read() || {}), [element.dataset.intentionId]: {
            status: received ? 'completed' : accepted ? 'awaiting_shipment' : 'rejected', events: [...current.events, event]
        } });
        return true;
    };
    const render = element => {
        const state = read(element);
        const badge = element.querySelector('[data-intention-status]');
        badge.textContent = labels[state.status];
        badge.className = 'donation-status donation-status--' + (state.status.startsWith('awaiting_') ? 'waiting' : state.status);
        const deadline = element.querySelector('[data-intention-deadline]');
        if (deadline) {
            deadline.hidden = state.status !== 'awaiting_acceptance';
            deadline.textContent = 'Aceite até ' + date(element.dataset.deadline);
        }
        return state;
    };
    window.resourceOngIntentions = { read, decide, render, date, labels };
})();
