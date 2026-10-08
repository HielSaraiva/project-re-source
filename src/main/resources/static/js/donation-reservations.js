(() => {
    const storage = window.resourceMockStorage.create('resource:mock:released-reservations', value =>
        value && typeof value === 'object' && Object.values(value).every(entry =>
            typeof entry?.protocol === 'string' && Number.isInteger(entry.availableQuantity)
            && entry.availableQuantity >= 0 && entry.needReservationReleased === true) ? value : null);
    const read = () => storage.read() || {};
    window.resourceDonationReservations = {
        release(protocol, releasedAt) {
            const reservations = read();
            // A terminal event releases the same reservation only once, including after reuse of its items.
            if (reservations[protocol]) return;
            storage.write({ ...reservations, [protocol]: {
                protocol, releasedAt, needReservationReleased: true, availableQuantity: 1,
                item: 'Monitor Dell 24"', category: 'Eletrônicos', condition: 'Usado (Bom estado)',
                description: 'Monitor funcionando, disponível para uma nova proposta.'
            } });
        },
        available(category) {
            return Object.values(read()).filter(entry => entry.category === category && entry.availableQuantity > 0);
        },
        reserve(protocol, quantity) {
            const reservations = read();
            const entry = reservations[protocol];
            if (!entry || !Number.isInteger(quantity) || quantity < 1 || quantity > entry.availableQuantity) return false;
            storage.write({ ...reservations, [protocol]: { ...entry, availableQuantity: entry.availableQuantity - quantity } });
            return true;
        }
    };
})();
