(() => {
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
    const elements = [...document.querySelectorAll('[data-reveal]')];
    if (!reducedMotion.matches && 'IntersectionObserver' in window) {
        const observer = new IntersectionObserver(entries => {
            for (const entry of entries) {
                if (!entry.isIntersecting) continue;
                entry.target.classList.remove('is-reveal-pending');
                observer.unobserve(entry.target);
            }
        }, {threshold: 0.08});
        for (const element of elements) {
            if (element.getBoundingClientRect().top <= window.innerHeight) continue;
            element.classList.add('is-reveal-pending');
            observer.observe(element);
        }
        reducedMotion.addEventListener('change', event => {
            if (!event.matches) return;
            observer.disconnect();
            elements.forEach(element => element.classList.remove('is-reveal-pending'));
        });
        window.addEventListener('beforeprint', () => {
            observer.disconnect();
            elements.forEach(element => element.classList.remove('is-reveal-pending'));
        });
    }

    const header = document.querySelector('.public-header');
    const indexLinks = [...document.querySelectorAll('.information-index nav a[href^="#"]')];
    if (indexLinks.length) {
        const sections = indexLinks.map(link => document.getElementById(link.hash.slice(1)));
        let scheduled = false;
        const updateIndex = () => {
            scheduled = false;
            const threshold = (header?.getBoundingClientRect().bottom ?? 0) + 40;
            let active = 0;
            sections.forEach((section, position) => {
                if (section.getBoundingClientRect().top <= threshold) active = position;
            });
            if (window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2) active = sections.length - 1;
            indexLinks.forEach((link, position) => {
                if (position === active) link.setAttribute('aria-current', 'location');
                else link.removeAttribute('aria-current');
            });
        };
        const scheduleUpdate = () => {
            if (scheduled) return;
            scheduled = true;
            requestAnimationFrame(updateIndex);
        };
        window.addEventListener('scroll', scheduleUpdate, {passive: true});
        window.addEventListener('resize', scheduleUpdate);
        updateIndex();
    }
    let navigationRequest = 0;
    document.querySelectorAll('a[href^="#"]').forEach(link => {
        link.addEventListener('click', async event => {
            if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
            const target = document.getElementById(link.hash.slice(1));
            if (!target) return;
            event.preventDefault();
            const request = ++navigationRequest;
            history.pushState(null, '', link.hash);
            const mobileMenu = document.querySelector('.nav-toggle[aria-expanded="true"]');
            if (mobileMenu) mobileMenu.click();
            const panel = header?.querySelector('.navigation-panel');
            const animations = panel?.getAnimations?.() ?? [];
            await Promise.allSettled(animations.map(animation => animation.finished));
            if (request !== navigationRequest) return;
            target.scrollIntoView({behavior: reducedMotion.matches ? 'instant' : 'smooth', block: 'start'});
            const temporaryFocus = !target.hasAttribute('tabindex');
            if (temporaryFocus) target.setAttribute('tabindex', '-1');
            target.focus({preventScroll: true});
            if (temporaryFocus) target.addEventListener('blur', () => target.removeAttribute('tabindex'), {once: true});
        });
    });
})();
