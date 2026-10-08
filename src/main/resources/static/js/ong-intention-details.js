(() => {
    const page = document.querySelector('[data-intention-detail]');
    const dialog = document.querySelector('#intention-decision');
    const result = document.querySelector('#intention-result');
    if (!page || typeof dialog?.showModal !== 'function') return;
    const api = window.resourceOngIntentions;
    const form = document.querySelector('#intention-decision-form');
    const reason = form.elements.reason;
    const receiptConfirmed = form.elements.receiptConfirmed;
    const confirm = form.querySelector('[data-confirm-decision]');
    const history = page.querySelector('[data-intention-history]');
    let choice = null;
    let trigger = null;
    const render = () => {
        const state = api.render(page);
        const deadline = page.querySelector('[data-intention-deadline]');
        if (state.status === 'awaiting_shipment' || state.status === 'awaiting_delivery') {
            const acceptedAt = state.events.find(event => event.title === 'Proposta aceita')?.at;
            const due = state.status === 'awaiting_delivery' ? page.dataset.inPersonDeadline
                : acceptedAt ? new Date(Date.parse(acceptedAt) + 7 * 24 * 60 * 60 * 1000).toISOString()
                : page.dataset.choiceDeadline;
            deadline.hidden = !due;
            if (due) deadline.textContent = (state.status === 'awaiting_delivery' ? 'Entrega presencial até ' : 'Escolha da modalidade até ') + api.date(due);
        }
        page.querySelector('[data-decision-actions]').hidden = state.status !== 'awaiting_acceptance';
        page.querySelector('[data-receipt-actions]').hidden = !['awaiting_ngo_confirmation', 'shipped'].includes(state.status);
        page.querySelector('[data-decision-note]').textContent = state.status === 'awaiting_acceptance'
            ? 'A ONG tem 7 dias para aceitar. Sem aceite, a proposta é cancelada. Após o aceite, o doador terá 7 dias para escolher a modalidade de entrega.'
            : state.status === 'awaiting_shipment' ? 'Proposta aceita. Aguarde o doador escolher a modalidade de entrega.'
            : state.status === 'awaiting_delivery' ? 'O doador tem 7 dias após a escolha da modalidade para confirmar a entrega presencial.'
            : state.status === 'awaiting_ngo_confirmation' ? 'O doador informou a entrega. Confira os itens e a quantidade recebida antes de confirmar o recebimento e concluir a doação.'
            : state.status === 'shipped' ? 'O doador informou a postagem pelos Correios. Confirme o recebimento somente após a encomenda chegar e os itens e a quantidade serem conferidos.'
            : state.status === 'completed' ? 'Recebimento confirmado pela ONG. A doação foi concluída e a confirmação está registrada no histórico.'
            : state.status === 'rejected' ? 'Proposta recusada. A decisão e seu motivo estão registrados no histórico.'
            : state.status === 'cancelled' ? 'A proposta foi cancelada e não pode mais ser aceita.'
            : 'Consulte os eventos abaixo para acompanhar esta doação.';
        history.querySelectorAll('[data-session-event]').forEach(event => event.remove());
        for (const event of state.events) {
            const li = document.createElement('li');
            li.dataset.sessionEvent = '';
            for (const [tag, value] of [['strong', event.title], ['time', api.date(event.at)], ['span', event.actor], ['p', event.description]]) {
                const element = document.createElement(tag);
                element.textContent = value;
                if (tag === 'time') element.dateTime = event.at;
                li.append(element);
            }
            history.append(li);
        }
        return state;
    };
    page.querySelectorAll('[data-open-decision]').forEach(button => button.addEventListener('click', () => {
        const receiving = button.dataset.openDecision === 'receive';
        const state = render();
        if (receiving ? !['awaiting_ngo_confirmation', 'shipped'].includes(state.status)
            : state.status !== 'awaiting_acceptance') return;
        trigger = button;
        choice = button.dataset.openDecision;
        const reject = choice === 'reject';
        form.reset(); reason.setCustomValidity(''); reason.required = reject;
        form.querySelector('[data-reason-field]').hidden = !reject;
        receiptConfirmed.required = receiving;
        form.querySelector('[data-receipt-attestation]').hidden = !receiving;
        form.querySelector('[data-postal-review]').hidden = !receiving || page.dataset.deliveryMethod !== 'carrier';
        document.querySelector('#decision-title').textContent = receiving ? 'Confirmar recebimento?' : reject ? 'Recusar proposta?' : 'Aceitar proposta?';
        document.querySelector('#decision-description').textContent = receiving
            ? 'Confirme somente após receber e conferir todos os itens abaixo. A doação passará para Concluído. Esta ação não pode ser desfeita.'
            : reject
            ? 'Esta decisão é definitiva. Informe o motivo da recusa antes de confirmar.'
            : 'Confira os itens e a quantidade. Ao aceitar, o doador terá 7 dias para escolher a modalidade de entrega. Esta decisão não pode ser desfeita por esta tela.';
        confirm.textContent = receiving ? 'Confirmar recebimento' : reject ? 'Confirmar recusa' : 'Confirmar aceite';
        confirm.classList.toggle('ui-button--danger', reject);
        confirm.classList.toggle('ui-button--primary', !reject);
        confirm.disabled = false;
        dialog.showModal();
    }));
    reason.addEventListener('input', () => reason.setCustomValidity(''));
    form.addEventListener('submit', event => {
        event.preventDefault();
        if (!dialog.open || !choice || confirm.disabled) return;
        reason.setCustomValidity(choice === 'reject' && !reason.value.trim() ? 'Informe o motivo da recusa.' : '');
        if (!form.reportValidity()) return;
        const accepted = choice === 'accept';
        const received = choice === 'receive';
        if (!api.decide(page, choice, reason.value)) { dialog.close(); render(); return; }
        confirm.disabled = true;
        render(); dialog.close();
        document.querySelector('#result-title').textContent = received ? 'Recebimento confirmado' : accepted ? 'Proposta aceita' : 'Proposta recusada';
        document.querySelector('#intention-result-description').textContent = received
            ? 'Obrigado! O recebimento foi confirmado e a doação passou para o status Concluído. A data, o responsável e a confirmação foram registrados no histórico.'
            : accepted
            ? 'O aceite foi registrado. A doação está Aguardando envio e o doador terá 7 dias para escolher como entregá-la.'
            : 'A recusa e seu motivo foram registrados no histórico desta doação.';
        result.showModal();
    });
    form.querySelector('[data-close-decision]').addEventListener('click', () => dialog.close());
    dialog.addEventListener('close', () => {
        if (dialog.open) return;
        choice = null;
        if (trigger && !trigger.closest('[hidden]')) trigger.focus();
    });
    result.querySelector('[data-close-result]').addEventListener('click', () => result.close());
    result.addEventListener('close', () => { history.tabIndex = -1; history.focus(); });
    render(); window.addEventListener('pageshow', render);
})();
