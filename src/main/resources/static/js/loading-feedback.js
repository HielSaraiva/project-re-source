(() => {
    const banner = document.createElement('div');
    banner.className = 'page-loading'; banner.hidden = true;
    banner.setAttribute('role', 'status'); banner.setAttribute('aria-live', 'polite');
    const spinner = document.createElement('span'); spinner.className = 'loading-spinner'; spinner.setAttribute('aria-hidden', 'true');
    const message = document.createElement('span'); message.textContent = 'Carregando página…';
    banner.append(spinner, message); document.body.append(banner);
    const activeButtons = new Map();
    const buttonBusy = button => {
        if (!button || activeButtons.has(button)) return () => {};
        const icon = spinner.cloneNode(true);
        const previousBusy = button.getAttribute('aria-busy');
        button.setAttribute('aria-busy', 'true');
        const timer = setTimeout(() => button.prepend(icon), 500);
        const finish = () => {
            clearTimeout(timer);
            icon.remove();
            if (previousBusy === null) button.removeAttribute('aria-busy');
            else button.setAttribute('aria-busy', previousBusy);
            activeButtons.delete(button);
        };
        activeButtons.set(button, finish);
        return finish;
    };
    let navigationTimer = null;
    const pageBusy = () => {
        if (navigationTimer !== null || !banner.hidden) return;
        document.querySelector('main')?.setAttribute('aria-busy', 'true');
        navigationTimer = setTimeout(() => {
            navigationTimer = null;
            (document.querySelector('dialog[open]') || document.body).append(banner);
            banner.hidden = false;
        }, 1500);
    };
    const reset = () => {
        clearTimeout(navigationTimer); navigationTimer = null;
        [...activeButtons.values()].forEach(finish => finish());
        banner.hidden = true; document.body.append(banner);
        document.querySelector('main')?.removeAttribute('aria-busy');
    };
    window.addEventListener('pageshow', reset);
    window.ResourceLoading = {
        startPage: pageBusy,
        navigate(target) { pageBusy(); window.location.assign(target); },
        reload() { pageBusy(); window.location.reload(); },
        request(container, button) {
            container.setAttribute('aria-busy', 'true');
            const status = document.createElement('p'); status.className = 'request-loading request-loading--accessible'; status.setAttribute('role', 'status');
            status.textContent = 'Processando solicitação…';
            container.append(status);
            const controls = [...container.querySelectorAll('button')].map(control => [control, control.disabled]);
            controls.forEach(([control]) => { control.disabled = true; });
            const finishButton = buttonBusy(button);
            const preventCancel = event => event.preventDefault(); container.addEventListener('cancel', preventCancel);
            const slow = setTimeout(() => {
                status.classList.remove('request-loading--accessible');
                status.textContent = 'A solicitação está demorando mais que o esperado. Aguarde a confirmação.';
            }, 10000);
            return () => {
                clearTimeout(slow);
                status.remove(); finishButton(); container.removeAttribute('aria-busy');
                container.removeEventListener('cancel', preventCancel);
                controls.forEach(([control, disabled]) => { control.disabled = disabled; });
            };
        }
    };
    document.addEventListener('click', event => {
        const link = event.target.closest('a[href]');
        if (!link || event.defaultPrevented || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey
                || link.hasAttribute('download') || (link.target && link.target !== '_self')) return;
        const target = new URL(link.href, location.href);
        if (target.origin !== location.origin || !['http:', 'https:'].includes(target.protocol)
                || (target.pathname === location.pathname && target.search === location.search && target.hash)) return;
        // Wait for the existing modal handlers to prevent navigation if needed.
        queueMicrotask(() => {
            if (!event.defaultPrevented) {
                pageBusy();
            }
        });
    });
    document.addEventListener('submit', event => {
        queueMicrotask(() => {
            if (!event.defaultPrevented && event.target.method.toLowerCase() === 'get') pageBusy();
        });
    });
})();
