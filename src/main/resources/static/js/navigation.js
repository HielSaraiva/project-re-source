(() => {
    // Set the initial CSS state before the header is parsed or painted.
    document.documentElement.dataset.navigationEnhanced = 'true';

    const initializeNavigation = () => {
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
            toggle.setAttribute('aria-label', open ? 'Fechar menu de navegação' : 'Abrir menu de navegação');
        };
        const close = () => setOpen(false);

        header.dataset.navigationReady = 'true';
        close();
        toggle.addEventListener('click', () => {
            header.dataset.navigationAnimated = 'true';
            setOpen(toggle.getAttribute('aria-expanded') !== 'true');
        });
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && toggle.getAttribute('aria-expanded') === 'true') {
                close();
                toggle.focus();
            }
        });
        navigation.addEventListener('click', (event) => {
            const link = event.target.closest('a[href]');
            if (!link || !mobile.matches || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;

            const destination = new URL(link.href, window.location.href);
            const currentPage = destination.origin === window.location.origin
                && destination.pathname === window.location.pathname
                && destination.search === window.location.search;

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
        document.addEventListener('DOMContentLoaded', initializeNavigation, {once: true});
    } else {
        initializeNavigation();
    }
})();
