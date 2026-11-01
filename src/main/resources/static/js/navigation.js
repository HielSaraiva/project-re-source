(() => {
    document.documentElement.dataset.navigationEnhanced = 'true';

    const initializeDonationReturn = () => {
        const context = window.ResourceHttp?.context || '';
        const allowed = [
            context + '/donor/donations',
            context + '/donor/dashboard',
        ];
        const key = 'resource:donation-return';
        const safe = (value) => {
            if (!value) return null;
            try {
                const target = new URL(value, location.origin);
                return target.origin === location.origin &&
                    allowed.includes(target.pathname)
                    ? target.pathname + target.search
                    : null;
            } catch {
                return null;
            }
        };
        document.addEventListener('click', (event) => {
            const link = event.target.closest('a[href]');
            if (
                link &&
                allowed.includes(location.pathname) &&
                link.origin === location.origin &&
                link.pathname.startsWith(context + '/donor/donation/')
            ) {
                try {
                    sessionStorage.setItem(
                        key,
                        location.pathname + location.search,
                    );
                } catch {
                    /* Optional navigation context. */
                }
            }
        });
        document.addEventListener('submit', (event) => {
            if (new URL(event.target.action).pathname === context + '/logout') {
                try {
                    sessionStorage.removeItem(key);
                } catch {
                    /* Optional storage. */
                }
            }
        });
        if (
            location.pathname.startsWith(context + '/donor/donation/') &&
            document.querySelector('[data-match-protocol]')
        ) {
            let back;
            try {
                back = safe(sessionStorage.getItem(key));
            } catch {
                /* Use the inventory fallback. */
            }
            const link = document.querySelector('.back-link');
            if (link) {
                link.href = back || allowed[0];
                link.textContent =
                    (back || allowed[0]).split('?')[0] === allowed[1]
                        ? '← Voltar ao painel'
                        : '← Voltar às minhas doações';
            }
        }
    };
    document.addEventListener('DOMContentLoaded', initializeDonationReturn, {
        once: true,
    });

    const initializeProfileMenus = () => {
        const reducedMotion = window.matchMedia(
            '(prefers-reduced-motion: reduce)',
        );
        const menus = [...document.querySelectorAll('.profile-menu')]
            .map((menu) => {
                const summary = menu.querySelector('summary');
                const panel = menu.querySelector('.profile-menu__logout');
                if (!summary || !panel) return null;
                let animation;
                const setOpen = (open, restoreFocus = false) => {
                    animation?.cancel();
                    animation = null;
                    summary.setAttribute('aria-expanded', String(open));
                    panel.inert = !open;
                    if (restoreFocus) summary.focus();
                    if (!open && !menu.open) return;
                    if (open) menu.open = true;
                    if (reducedMotion.matches || !panel.animate) {
                        menu.open = open;
                        return;
                    }
                    const frames = [
                        {
                            opacity: 0,
                            transform: 'translateY(-6px) scale(.98)',
                        },
                        { opacity: 1, transform: 'translateY(0) scale(1)' },
                    ];
                    animation = panel.animate(
                        open ? frames : frames.toReversed(),
                        {
                            duration: open ? 180 : 120,
                            easing: 'ease-out',
                            fill: 'both',
                        },
                    );
                    const current = animation;
                    current.finished
                        .then(() => {
                            if (animation !== current) return;
                            menu.open = open;
                            current.cancel();
                            animation = null;
                        })
                        .catch(() => {});
                };
                summary.setAttribute('aria-expanded', String(menu.open));
                panel.inert = !menu.open;
                summary.addEventListener('click', (event) => {
                    event.preventDefault();
                    const open =
                        summary.getAttribute('aria-expanded') !== 'true';
                    if (open)
                        menus.forEach((other) => {
                            if (other && other.menu !== menu) other.close();
                        });
                    setOpen(open);
                });
                return {
                    menu,
                    close: (restoreFocus = false) =>
                        setOpen(false, restoreFocus),
                };
            })
            .filter(Boolean);
        document.addEventListener('pointerdown', (event) => {
            menus.forEach((entry) => {
                if (!entry.menu.contains(event.target)) entry.close();
            });
        });
        document.addEventListener('focusin', (event) => {
            menus.forEach((entry) => {
                if (!entry.menu.contains(event.target)) entry.close();
            });
        });
        document.addEventListener('keydown', (event) => {
            if (event.key !== 'Escape') return;
            menus.forEach((entry) => {
                if (
                    entry.menu
                        .querySelector('summary')
                        .getAttribute('aria-expanded') === 'true'
                ) {
                    event.preventDefault();
                    entry.close(true);
                }
            });
        });
    };

    const initializeNavigation = () => {
        initializeProfileMenus();
        const header = document.querySelector('.navbar');
        const toggle = header?.querySelector('.nav-toggle');
        const navigation = header?.querySelector('.nav-links');
        if (!toggle || !navigation) return;

        const mobile = window.matchMedia('(max-width: 1100px)');
        const setOpen = (open) => {
            header.dataset.navigationOpen = String(open);
            navigation.classList.toggle('is-open', open);
            navigation.inert = mobile.matches && !open;
            toggle.setAttribute('aria-expanded', String(open));
            toggle.setAttribute(
                'aria-label',
                open ? 'Fechar menu de navegação' : 'Abrir menu de navegação',
            );
        };
        const close = () => setOpen(false);

        header.dataset.navigationReady = 'true';
        close();
        toggle.addEventListener('click', () => {
            header.dataset.navigationAnimated = 'true';
            setOpen(toggle.getAttribute('aria-expanded') !== 'true');
        });
        document.addEventListener('keydown', (event) => {
            if (
                event.key === 'Escape' &&
                toggle.getAttribute('aria-expanded') === 'true'
            ) {
                close();
                toggle.focus();
            }
        });
        navigation.addEventListener('click', (event) => {
            const link = event.target.closest('a[href]');
            if (
                !link ||
                !mobile.matches ||
                event.ctrlKey ||
                event.metaKey ||
                event.shiftKey ||
                event.altKey
            )
                return;

            const destination = new URL(link.href, window.location.href);
            const currentPage =
                destination.origin === window.location.origin &&
                destination.pathname === window.location.pathname &&
                destination.search === window.location.search;

            if (currentPage && destination.hash && link.target !== '_blank') {
                close();
                toggle.focus();
            }
        });
        document.addEventListener('click', (event) => {
            if (!header.contains(event.target)) close();
        });
        mobile.addEventListener('change', () => {
            header.dataset.navigationAnimated = 'false';
            close();
        });
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeNavigation, {
            once: true,
        });
    } else {
        initializeNavigation();
    }
})();
