(() => {
    const menu = document.querySelector('[data-notifications]');
    if (!menu) return;
    const trigger = menu.querySelector('summary');
    const panel = menu.querySelector('.notification-panel');
    const badge = menu.querySelector('[data-notification-count]');
    const list = menu.querySelector('[data-notification-list]');
    const status = menu.querySelector('[data-notification-status]');
    const readAll = menu.querySelector('[data-read-all]');
    const previous = menu.querySelector('[data-notification-previous]');
    const next = menu.querySelector('[data-notification-next]');
    const retry = menu.querySelector('[data-notification-retry]');
    const context = (document.querySelector('meta[name="context-path"]')?.content || '/').replace(/\/$/, '');
    const endpoint = `${context}/api/notifications`;
    let page = 0, loading = false, mutating = false;
    panel.inert = true;
    const close = (restoreFocus = false) => {
        menu.open = false;
        if (restoreFocus) trigger.focus();
    };
    const request = async (url, method = 'GET') => {
        const headers = {Accept: 'application/json'};
        if (method === 'POST') {
            headers[document.querySelector('meta[name="_csrf_header"]').content] = document.querySelector('meta[name="_csrf"]').content;
        }
        const response = await fetch(url, {method, headers, credentials: 'same-origin', cache: 'no-store'});
        if (!response.ok || response.redirected) throw new Error('request_failed');
        return method === 'GET' ? response.json() : null;
    };
    const setBadge = (count) => {
        badge.hidden = count === 0;
        badge.textContent = count > 99 ? '99+' : String(count);
        trigger.setAttribute('aria-label', count ? `Notificações: ${count} não lidas` : 'Notificações: nenhuma não lida');
        readAll.disabled = count === 0 || mutating;
    };
    const showError = () => {
        status.hidden = false;
        status.textContent = 'Não foi possível carregar suas notificações. Tente novamente.';
        retry.hidden = false;
    };
    const load = async () => {
        if (loading || mutating) return;
        loading = true;
        retry.hidden = true;
        try {
            const data = await request(`${endpoint}?page=${page}`);
            setBadge(data.unreadCount);
            list.replaceChildren();
            for (const notice of data.items) {
                const li = document.createElement('li');
                li.className = `notification-item${notice.read ? '' : ' notification-item--unread'}`;
                const a = document.createElement('a');
                a.href = context + notice.targetPath;
                const title = document.createElement('strong'); title.textContent = notice.title;
                const text = document.createElement('p'); text.textContent = notice.message;
                const date = document.createElement('time'); date.dateTime = notice.createdAt;
                date.textContent = new Intl.DateTimeFormat('pt-BR', {dateStyle: 'short', timeStyle: 'short'}).format(new Date(notice.createdAt));
                a.append(title, text, date); li.append(a); list.append(li);
                a.addEventListener('click', async (event) => {
                    if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
                    event.preventDefault();
                    if (mutating) return;
                    mutating = true;
                    try {
                        await request(`${endpoint}/${notice.id}/read`, 'POST');
                        location.assign(a.href);
                    } catch { mutating = false; showError(); }
                });
            }
            status.hidden = data.items.length > 0;
            status.textContent = page === 0 ? 'Você está em dia! As principais atualizações das suas doações aparecerão aqui.' : 'Não há mais notificações nesta página.';
            menu.querySelector('.notification-panel__footer').hidden = data.items.length === 0 && page === 0;
            previous.disabled = page === 0;
            next.disabled = !data.hasMore;
            menu.querySelector('[data-notification-page]').textContent = `Página ${page + 1}`;
        } catch { if (menu.open) showError(); }
        finally { loading = false; }
    };
    menu.addEventListener('toggle', () => {
        trigger.setAttribute('aria-expanded', String(menu.open));
        panel.inert = !menu.open;
        if (menu.open) {
            document.querySelectorAll('.profile-menu[open]').forEach(other => { other.querySelector('summary').click(); });
            load();
        }
    });
    document.addEventListener('pointerdown', event => { if (!menu.contains(event.target)) close(); });
    document.addEventListener('focusin', event => { if (!menu.contains(event.target)) close(); });
    document.addEventListener('keydown', event => { if (event.key === 'Escape' && menu.open) { event.preventDefault(); close(true); } });
    retry.addEventListener('click', load);
    previous.addEventListener('click', () => { if (!loading && !mutating) { page--; load(); } });
    next.addEventListener('click', () => { if (!loading && !mutating) { page++; load(); } });
    readAll.addEventListener('click', async () => {
        if (mutating) return;
        mutating = true; readAll.disabled = true;
        try { await request(`${endpoint}/read-all`, 'POST'); }
        catch { mutating = false; showError(); readAll.disabled = false; return; }
        mutating = false; await load();
    });
    load();
    setInterval(() => { if (!document.hidden && !menu.open) load(); }, 60000);
    document.addEventListener('visibilitychange', () => { if (!document.hidden) load(); });
})();
