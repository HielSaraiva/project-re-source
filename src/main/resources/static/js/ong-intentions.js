(() => {
    const page = document.querySelector('[data-intention-list], [data-intention-dashboard]');
    if (!page) return;
    const rows = [...page.querySelectorAll('[data-intention-id]')];
    const api = window.resourceOngIntentions;
    const dashboard = page.hasAttribute('data-intention-dashboard');
    const form = document.querySelector('#intention-filters');
    const normalize = value => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
    const filterStorage = window.resourceMockStorage.create('resource:mock:ong:intention-filters', value =>
        value && typeof value.query === 'string' && typeof value.status === 'string' && typeof value.order === 'string'
            && Number.isSafeInteger(value.page) && value.page > 0 ? value : null);
    let currentPage = 1;
    const pageSize = 6;
    const render = () => {
        const states = rows.map(row => api.render(row));
        if (dashboard) {
            let shown = 0;
            rows.forEach((row, i) => { row.hidden = states[i].status !== 'awaiting_acceptance' || shown++ >= 3; });
            page.querySelector('[data-pending-count]').textContent = states.filter(state => state.status === 'awaiting_acceptance').length;
            page.querySelector('[data-received-count]').textContent = states.filter(state => state.status === 'completed').length;
            page.querySelector('[data-dashboard-empty]').hidden = shown > 0;
            return;
        }
        const query = normalize(form.elements.query.value.trim());
        const status = form.elements.status.value;
        const order = form.elements.order.value;
        const visible = rows.filter((row, index) => (!status || states[index].status === status)
            && normalize(row.dataset.search).includes(query));
        visible.sort((a, b) => order === 'deadline' ? (api.read(a).status === 'awaiting_acceptance' ? 0 : 1) - (api.read(b).status === 'awaiting_acceptance' ? 0 : 1) || Date.parse(a.dataset.deadline) - Date.parse(b.dataset.deadline)
            : Date.parse(b.dataset.createdAt) - Date.parse(a.dataset.createdAt));
        const totalPages = Math.max(1, Math.ceil(visible.length / pageSize));
        currentPage = Math.min(currentPage, totalPages);
        rows.forEach(row => { row.hidden = true; });
        visible.forEach(row => page.querySelector('[data-intention-rows]').append(row));
        visible.slice((currentPage - 1) * pageSize, currentPage * pageSize).forEach(row => { row.hidden = false; });
        document.querySelector('[data-results-count]').textContent = `${visible.length} ${visible.length === 1 ? 'doação encontrada' : 'doações encontradas'}`;
        document.querySelector('[data-empty-results]').hidden = visible.length > 0;
        document.querySelector('[data-pagination]').hidden = visible.length === 0;
        document.querySelector('[data-pagination]').setAttribute('aria-label', `Paginação das doações: página ${currentPage} de ${totalPages}`);
        const numbers = document.querySelector('[data-page-numbers]');
        numbers.replaceChildren();
        let previous = 0;
        for (let page = 1; page <= totalPages; page++) {
            if (page > 3 && page !== totalPages && Math.abs(page - currentPage) > 1) continue;
            if (previous && page - previous > 1) {
                const ellipsis = document.createElement('span');
                ellipsis.textContent = '...';
                ellipsis.setAttribute('aria-hidden', 'true');
                numbers.append(ellipsis);
            }
            const button = document.createElement('button');
            button.type = 'button';
            button.textContent = page;
            button.dataset.page = page;
            button.setAttribute('aria-label', `Página ${page}`);
            if (page === currentPage) {
                button.className = 'current';
                button.setAttribute('aria-current', 'page');
            }
            numbers.append(button);
            previous = page;
        }
        document.querySelector('[data-previous-page]').disabled = currentPage === 1;
        document.querySelector('[data-next-page]').disabled = currentPage === totalPages;
        filterStorage.write({ query: form.elements.query.value, status, order, page: currentPage });
        const params = new URLSearchParams();
        if (form.elements.query.value) params.set('query', form.elements.query.value);
        if (status) params.set('status', status);
        if (order !== 'recent') params.set('order', order);
        if (currentPage > 1) params.set('page', currentPage);
        history.replaceState(null, '', location.pathname + (params.size ? '?' + params.toString() : ''));
        document.querySelector('[data-pending-count]').textContent = states.filter(state => state.status === 'awaiting_acceptance').length;
    };
    if (form) {
        const params = new URLSearchParams(location.search);
        const saved = params.size ? null : filterStorage.read();
        for (const name of ['query', 'status', 'order']) {
            if (params.has(name)) form.elements[name].value = params.get(name);
            else if (saved) form.elements[name].value = saved[name];
        }
        const requestedPage = Number(params.get('page') || saved?.page || 1);
        currentPage = Number.isSafeInteger(requestedPage) && requestedPage > 0 ? requestedPage : 1;
        if (!form.elements.order.value) form.elements.order.value = 'recent';
        const filter = () => { currentPage = 1; render(); };
        form.addEventListener('submit', event => { event.preventDefault(); filter(); });
        form.elements.query.addEventListener('input', filter);
        form.elements.status.addEventListener('change', filter);
        form.elements.order.addEventListener('change', filter);
        document.querySelector('[data-clear-filters]').addEventListener('click', () => { form.reset(); filter(); form.elements.query.focus(); });
        document.querySelector('[data-page-numbers]').addEventListener('click', event => {
            const button = event.target.closest('button[data-page]');
            if (!button) return;
            currentPage = Number(button.dataset.page);
            render();
            document.querySelector('[data-results-count]').focus();
        });
        for (const [selector, change] of [['[data-previous-page]', -1], ['[data-next-page]', 1]])
            document.querySelector(selector).addEventListener('click', () => { currentPage += change; render(); document.querySelector('[data-results-count]').focus(); });
    }
    render();
    window.addEventListener('pageshow', render);
})();
