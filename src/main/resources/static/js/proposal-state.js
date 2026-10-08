(() => {
    const storage = window.resourceMockStorage.create('resource:mock:monitor-associacao:proposal', state =>
        state?.cancelled === true && typeof state.inventoryAvailable === 'boolean' ? state : null);
    const write = state => {
        storage.write(state);
        window.dispatchEvent(new Event('resource:donation-updated'));
    };
    const read = () => {
        let state = storage.read();
        if (!state?.cancelled && window.resourceDonationDeadlines.expired(window.resourceDonationDeadlines.stage('acceptance'))) {
            state = { cancelled: true, inventoryAvailable: true, cancelledAt: window.resourceDonationDeadlines.stage('acceptance'), needReservationReleased: true, reason: 'acceptance_expired' };
            storage.write(state);
        }
        return state;
    };
    window.resourceProposalState = {
        read,
        cancel() {
            if (read()?.cancelled) return false;
            write({ cancelled: true, inventoryAvailable: true, needReservationReleased: true, cancelledAt: new Date().toISOString() });
            return true;
        },
        reserve() {
            const state = read();
            if (!state?.inventoryAvailable) return false;
            write({ ...state, inventoryAvailable: false });
            return true;
        }
    };
})();
