(() => {
    const validate = state => {
        if (state?.status === 'cancelled') return state;
        if (state?.method === 'carrier') {
            if (state.status === 'awaiting_shipping') return state;
            if (state.status === 'shipped' && /^\d{4}-\d{2}-\d{2}$/.test(state.date)
                && /^[A-Z]{2}\d{9}[A-Z]{2}$/.test(state.trackingCode) && typeof state.notes === 'string') return state;
            return null;
        }
        if (state?.method !== 'in_person') return null;
        if (state.status === 'awaiting_delivery') return state;
        if (state.status === 'awaiting_ngo_confirmation' && /^\d{4}-\d{2}-\d{2}$/.test(state.date)
            && typeof state.recipient === 'string' && typeof state.notes === 'string') return state;
        return null;
    };
    const stores = new Map();
    const page = () => document.querySelector('[data-delivery-protocol]');
    const protocol = () => page()?.dataset.deliveryProtocol || 'INT-2026-202';
    const storage = () => {
        const key = protocol() === 'INT-2026-202' ? 'resource:mock:monitor-instituto:delivery' : 'resource:mock:delivery:' + protocol();
        if (!stores.has(key)) stores.set(key, window.resourceMockStorage.create(key, validate));
        return stores.get(key);
    };
    const dates = window.resourceDonationDeadlines;
    const choiceDeadline = () => page()?.dataset.expiredStage ? page().dataset.choiceDeadline : dates.stage('choice');
    const dueFor = state => state?.method ? dates.afterWeek(state.methodConfirmedAt) : choiceDeadline();
    const locked = state => ['cancelled', 'shipped', 'awaiting_ngo_confirmation'].includes(state?.status);
    const release = state => {
        window.resourceDonationReservations.release(protocol(), state.cancelledAt);
        return state;
    };
    const read = () => {
        let state = storage().read();
        const initialStage = page()?.dataset.expiredStage;
        if (!state && ['in_person', 'carrier'].includes(initialStage)) {
            state = { method: initialStage, status: initialStage === 'carrier' ? 'awaiting_shipping' : 'awaiting_delivery',
                methodConfirmedAt: page().dataset.methodConfirmedAt };
        }
        if (state?.method && !Number.isFinite(Date.parse(state.methodConfirmedAt))) {
            state = { ...state, methodConfirmedAt: new Date().toISOString() };
            storage().write(state);
        }
        if (!locked(state) && dates.expired(dueFor(state))) {
            state = { ...state, status: 'cancelled', reason: 'deadline_expired',
                expiryStage: state?.method || 'choice', cancelledAt: dueFor(state),
                inventoryAvailable: true, needReservationReleased: true };
            storage().write(state);
        }
        if (state?.status === 'cancelled') return release(state);
        return state;
    };
    const write = next => {
        const previous = read();
        if (locked(previous) || (previous?.method && next.method && next.method !== previous.method)) return false;
        let state = { ...previous, ...next };
        if (state.status === 'cancelled') {
            state = { ...state, cancelledAt: new Date().toISOString(), inventoryAvailable: true, needReservationReleased: true };
            release(state);
        } else if (state.method) {
            state.methodConfirmedAt = previous?.methodConfirmedAt || new Date().toISOString();
        }
        if (['shipped', 'awaiting_ngo_confirmation'].includes(state.status)) state.reportedAt = new Date().toISOString();
        if (!validate(state)) return false;
        storage().write(state);
        window.dispatchEvent(new Event('resource:donation-updated'));
        return true;
    };
    const deadline = () => dueFor(read());
    const canConfirm = form => {
        const state = read();
        if (locked(state) || !state?.method) return false;
        const due = dueFor(state);
        dates.render(document.querySelector('[data-active-deadline]'), due,
            'Modalidade escolhida em ' + dates.date(state.methodConfirmedAt), '7 dias após a escolha');
        const date = form?.elements.namedItem('date');
        if (date) {
            date.min = dates.inputDate(state.methodConfirmedAt);
            date.max = [dates.inputDate(new Date().toISOString()), dates.inputDate(due)].sort()[0];
        }
        return !dates.expired(due);
    };
    window.resourceDeliveryState = { read, write, deadline, canConfirm };
})();
