(() => {
    const week = 7 * 24 * 60 * 60 * 1000;
    const storage = window.resourceMockStorage.create('resource:mock:donation-deadlines', state =>
        state && typeof state === 'object' && Object.values(state).every(value => Number.isFinite(Date.parse(value))) ? state : null);
    const date = value => new Date(value).toLocaleDateString('pt-BR', { timeZone: 'America/Fortaleza' });
    const inputDate = value => new Intl.DateTimeFormat('en-CA', {
        timeZone: 'America/Fortaleza', year: 'numeric', month: '2-digit', day: '2-digit'
    }).format(new Date(value));
    const afterWeek = value => new Date(Date.parse(value) + week).toISOString();
    const startedAt = value => new Date(Date.parse(value) - week).toISOString();
    const expired = value => value && Date.now() >= Date.parse(value);
    const stage = name => {
        const state = storage.read() || {};
        if (state[name]) return state[name];
        const initial = document.querySelector(`[data-${name}-deadline]`)?.dataset[name + 'Deadline'];
        if (!initial || !Number.isFinite(Date.parse(initial))) return null;
        storage.write({ ...state, [name]: initial });
        return initial;
    };
    const render = (element, deadline, eventLabel, suffix) => {
        if (!element || !deadline) return;
        element.querySelector('span').textContent = `Até ${date(deadline)} (${suffix})`;
        element.querySelector('small').textContent = (expired(deadline) ? 'Prazo encerrado. ' : '') + eventLabel;
    };
    window.resourceDonationDeadlines = { afterWeek, startedAt, expired, stage, date, inputDate, render };
})();
