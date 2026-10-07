(() => {
    const storage = window.resourceMockStorage.create('resource:mock:monitor-associacao:proposal', state =>
        state?.cancelled === true && typeof state.inventoryAvailable === 'boolean' ? state : null);
    const write = storage.write;
    const read = () => {
        let state = storage.read();
        if (!state?.cancelled && window.resourceDonationDeadlines.expired(window.resourceDonationDeadlines.stage('acceptance'))) {
            state = { cancelled: true, inventoryAvailable: true, cancelledAt: new Date().toISOString(), reason: 'acceptance_expired' };
            write(state);
        }
        return state;
    };
    window.resourceProposalState = {
        read,
        cancel() {
            if (read()?.cancelled) return false;
            write({ cancelled: true, inventoryAvailable: true, cancelledAt: new Date().toISOString() });
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
