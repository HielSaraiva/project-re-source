(() => {
    const initialized = new WeakMap();
    const reducedMotion = () => matchMedia('(prefers-reduced-motion: reduce)').matches;
    const initialize = root => {
        const form = root.querySelector('.auth-form');
        if (!form || initialized.has(form)) return;
        const cnpj = form.querySelector('#cnpj');
        cnpj?.addEventListener('blur', () => {
            const value = cnpj.value.trim().toUpperCase();
            if (/^[A-Z0-9]{12}[0-9]{2}$/.test(value)) {
                cnpj.value = value.replace(/^([A-Z0-9]{2})([A-Z0-9]{3})([A-Z0-9]{3})([A-Z0-9]{4})([0-9]{2})$/, '$1.$2.$3/$4-$5');
            }
        });
        const passwordControls = [...form.querySelectorAll('.auth-password-toggle')].map(toggle => {
            const input = form.querySelector('#' + CSS.escape(toggle.getAttribute('aria-controls')));
            const label = toggle.getAttribute('aria-label');
            toggle.hidden = false;
            toggle.addEventListener('pointerdown', event => event.preventDefault());
            let animation;
            toggle.addEventListener('click', () => {
                const selection = [input.selectionStart, input.selectionEnd];
                const restoreCursor = document.activeElement === input;
                const visible = input.type === 'password';
                input.type = visible ? 'text' : 'password';
                toggle.setAttribute('aria-pressed', String(visible));
                toggle.setAttribute('aria-label', visible ? label.replace(/^Mostrar/, 'Ocultar') : label);
                if (restoreCursor) { input.focus({ preventScroll: true }); input.setSelectionRange(...selection); }
                animation?.cancel();
                if (!reducedMotion()) animation = toggle.querySelector('.auth-password-icon').animate(
                    [{ transform: 'scale(1)' }, { transform: 'scale(.8)' }, { transform: 'scale(1)' }], { duration: 200 });
            });
            return { input, toggle, label };
        });
        const password = form.querySelector('#password');
        const confirmation = form.querySelector('#passwordConfirmation');
        const name = form.querySelector('#fullName, #representativeFullName');
        const validateRegistration = () => {
            if (!confirmation) return;
            confirmation.setCustomValidity(confirmation.value && confirmation.value !== password.value ? 'As senhas devem ser iguais.' : '');
            password.setCustomValidity('');
            password.setCustomValidity(new TextEncoder().encode(password.value).length > Number(password.dataset.passwordMaxBytes)
                ? password.dataset.passwordByteLimitMessage
                : password.validity.patternMismatch ? form.querySelector('#password-help').textContent : '');
            name?.setCustomValidity(name.value && !name.value.trim() ? 'Informe seu nome completo.' : '');
        };
        if (confirmation) {
            [password, confirmation, name].filter(Boolean).forEach(input => input.addEventListener('input', validateRegistration));
            form.querySelector('[data-registration-errors]')?.focus();
        }
        let submitting = false;
        let finishLoading;
        form.addEventListener('submit', event => {
            validateRegistration();
            if (!form.reportValidity()) { event.preventDefault(); return; }
            if (submitting) { event.preventDefault(); return; }
            submitting = true;
            finishLoading = window.ResourceLoading?.request(form, event.submitter);
        });
        const reset = () => {
            submitting = false;
            finishLoading?.();
            finishLoading = null;
            passwordControls.forEach(({ input, toggle, label }) => {
                input.type = 'password';
                toggle.setAttribute('aria-pressed', 'false');
                toggle.setAttribute('aria-label', label);
            });
            validateRegistration();
        };
        initialized.set(form, reset);
        reset();
    };
    initialize(document);
    window.addEventListener('pageshow', () => initialized.get(document.querySelector('.auth-form'))?.());

    const nav = document.querySelector('.auth-profiles');
    const card = document.querySelector('.auth-card');
    if (!nav || !card) return;
    const links = [...nav.querySelectorAll('.auth-profile')];
    const key = target => { const url = new URL(target, location.href); return url.pathname + url.search; };
    let activeKey = key(nav.querySelector('.is-selected').href);
    let sequence = 0;
    const cache = new Map();
    const pending = new Map();
    const heading = card.querySelector('.auth-heading');
    const snapshot = () => ({ content: card.querySelector('.auth-content'), title: document.title,
        heading: heading.querySelector('h1').textContent, subtitle: heading.querySelector('p').textContent });
    cache.set(activeKey, snapshot());
    const notice = document.createElement('p');
    notice.className = 'auth-help auth-switch-status';
    notice.setAttribute('role', 'status'); notice.setAttribute('aria-live', 'polite');
    nav.after(notice);
    nav.classList.add('is-enhanced');
    const select = target => {
        links.forEach((link, index) => {
            const selected = key(link.href) === target;
            link.classList.toggle('is-selected', selected);
            if (selected) { link.setAttribute('aria-current', 'page'); nav.style.setProperty('--auth-profile-index', index); }
            else link.removeAttribute('aria-current');
        });
    };
    select(activeKey);
    history.replaceState({ ...history.state, authProfile: activeKey }, '', location.href);
    const load = target => {
        if (cache.has(target)) return Promise.resolve(cache.get(target));
        if (pending.has(target)) return pending.get(target);
        const request = (async () => {
            const response = await fetch(target, { credentials: 'same-origin', signal: AbortSignal.timeout(10000) });
            if (!response.ok || response.redirected) throw new Error('profile unavailable');
            const page = new DOMParser().parseFromString(await response.text(), 'text/html');
            const content = page.querySelector('.auth-content');
            const title = page.querySelector('.auth-heading');
            if (!content || !title) throw new Error('invalid profile');
            const entry = { content: document.adoptNode(content), title: page.title,
                heading: title.querySelector('h1').textContent, subtitle: title.querySelector('p').textContent };
            cache.set(target, entry);
            return entry;
        })().finally(() => pending.delete(target));
        pending.set(target, request);
        return request;
    };
    const animations = [];
    const switchProfile = async (target, push = true) => {
        const current = ++sequence;
        notice.textContent = '';
        select(target);
        nav.setAttribute('aria-busy', 'true');
        const timer = setTimeout(() => { if (current === sequence) notice.textContent = 'Carregando formulário…'; }, 500);
        try {
            const entry = await load(target);
            if (current !== sequence) return;
            if (target === activeKey) return;
            cache.set(activeKey, snapshot());
            initialized.get(card.querySelector('.auth-form'))?.();
            animations.splice(0).forEach(animation => animation.cancel());
            card.querySelector('.auth-content').replaceWith(entry.content);
            heading.querySelector('h1').textContent = entry.heading;
            heading.querySelector('p').textContent = entry.subtitle;
            document.title = entry.title;
            const direction = links.findIndex(link => key(link.href) === target) > links.findIndex(link => key(link.href) === activeKey) ? 1 : -1;
            activeKey = target;
            initialize(card);
            document.dispatchEvent(new CustomEvent('auth:profilechange', { detail: { root: entry.content } }));
            if (push) history.pushState({ ...history.state, authProfile: target }, '', target);
            if (!reducedMotion()) {
                [heading, entry.content].forEach(element => animations.push(element.animate(
                    [{ opacity: 0, transform: `translateX(${direction * 12}px)` }, { opacity: 1, transform: 'translateX(0)' }],
                    { duration: 260, easing: 'cubic-bezier(.22, 1, .36, 1)' })));
            }
        } catch {
            if (current !== sequence) return;
            select(activeKey);
            notice.textContent = 'Não foi possível abrir o formulário. Selecione o perfil para tentar novamente.';
            if (!push) history.replaceState({ ...history.state, authProfile: activeKey }, '', activeKey);
        } finally {
            clearTimeout(timer);
            if (current === sequence) {
                nav.removeAttribute('aria-busy');
                if (notice.textContent === 'Carregando formulário…') notice.textContent = '';
            }
        }
    };
    nav.addEventListener('click', event => {
        const link = event.target.closest('.auth-profile');
        if (!link || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
        event.preventDefault();
        if (card.querySelector('.auth-form[aria-busy="true"]')) return;
        switchProfile(key(link.href));
    });
    nav.addEventListener('keydown', event => {
        if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;
        const index = links.indexOf(document.activeElement);
        if (index < 0) return;
        event.preventDefault();
        const next = event.key === 'Home' ? 0 : event.key === 'End' ? links.length - 1
            : (index + (event.key === 'ArrowRight' ? 1 : -1) + links.length) % links.length;
        links[next].focus(); links[next].click();
    });
    window.addEventListener('popstate', event => {
        if (event.state?.authProfile && links.some(link => key(link.href) === event.state.authProfile))
            switchProfile(event.state.authProfile, false);
    });
    // Keep the other form ready without navigating or rerendering the shared page.
    links.filter(link => key(link.href) !== activeKey).forEach(link => load(key(link.href)).catch(() => {}));
})();
