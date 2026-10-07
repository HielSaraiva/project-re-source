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
    const storage = window.resourceMockStorage.create('resource:mock:monitor-instituto:delivery', validate);
    const read = () => {
        const state = storage.read();
        if (state?.method && !Number.isFinite(Date.parse(state.methodConfirmedAt))) {
            const upgraded = { ...state, methodConfirmedAt: new Date().toISOString() };
            storage.write(upgraded);
            return upgraded;
        }
        return state;
    };
    const write = state => {
        const previous = read();
        return storage.write(state.method ? {
            ...state,
            methodConfirmedAt: previous?.methodConfirmedAt || new Date().toISOString()
        } : state);
    };
    const deadline = () => {
        const state = read();
        return state?.method ? window.resourceDonationDeadlines.afterWeek(state.methodConfirmedAt)
            : window.resourceDonationDeadlines.stage('choice');
    };
    const canConfirm = form => {
        const state = read();
        if (!state?.method) return true;
        const dates = window.resourceDonationDeadlines;
        const due = deadline();
        dates.render(document.querySelector('[data-active-deadline]'), due,
            'Modalidade escolhida em ' + dates.date(state.methodConfirmedAt), '7 dias após a escolha');
        const date = form?.elements.namedItem('date');
        if (date) {
            date.min = dates.inputDate(state.methodConfirmedAt);
            date.max = [dates.inputDate(new Date().toISOString()), dates.inputDate(due)].sort()[0];
        }
        if (!dates.expired(due)) return true;
        form?.querySelectorAll('input, textarea, button').forEach(control => { control.disabled = true; });
        return false;
    };
    window.resourceDeliveryState = { read, write, deadline, canConfirm };
})();
