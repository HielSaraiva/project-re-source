(() => {
    const initialized = new WeakSet();
    const initialize = root => {
        const form = root.querySelector('[data-cnpj-lookup]');
        if (!form || initialized.has(form)) return;
        initialized.add(form);
        const cnpj = form.querySelector('#cnpj');
        const legalName = form.querySelector('#legalName');
        const search = form.querySelector('.cnpj-search');
        const status = form.querySelector('#cnpj-lookup-status');
        const canonical = () => cnpj.value.trim().toUpperCase().replace(/[./-]/g, '');
        let request;
        let sequence = 0;
        let verified = legalName.value ? canonical() : null;
        search.hidden = false;
        const lookup = async () => {
            if (!cnpj.reportValidity()) return;
            const value = canonical();
            if (verified === value && legalName.value) return;
            request?.abort();
            request = new AbortController();
            const controller = request;
            const current = ++sequence;
            const timer = setTimeout(() => controller.abort(), 10000);
            search.setAttribute('aria-busy', 'true');
            status.textContent = 'Consultando CNPJ…';
            status.className = 'auth-help';
            try {
                const target = new URL(form.dataset.cnpjLookup, location.origin);
                target.searchParams.set('cnpj', value);
                const response = await fetch(target, { headers: { Accept: 'application/json' }, signal: controller.signal, credentials: 'same-origin' });
                const body = await response.json();
                if (current !== sequence || canonical() !== value) return;
                if (!response.ok) throw new Error(body.detail || 'Não foi possível consultar o CNPJ. Tente novamente.');
                legalName.value = body.legalName;
                verified = value;
                status.textContent = 'CNPJ ativo. Razão social preenchida.';
            } catch (error) {
                if (current !== sequence || canonical() !== value) return;
                verified = null;
                legalName.value = '';
                status.className = 'auth-field-error';
                status.textContent = error.name === 'AbortError' || error instanceof TypeError
                    ? 'Consulta indisponível no momento. Tente novamente.' : error.message;
            } finally {
                clearTimeout(timer);
                if (current === sequence) search.removeAttribute('aria-busy');
            }
        };
        search.addEventListener('click', lookup);
        cnpj.addEventListener('input', () => {
            ++sequence;
            request?.abort();
            search.removeAttribute('aria-busy');
            verified = null;
            legalName.value = '';
            status.textContent = '';
        });
        cnpj.addEventListener('blur', () => { if (cnpj.value && cnpj.checkValidity()) lookup(); });
        const cpf = form.querySelector('#representativeCpf');
        cpf.addEventListener('blur', () => {
            const value = cpf.value.trim();
            if (/^[0-9]{11}$/.test(value)) cpf.value = value.replace(/^([0-9]{3})([0-9]{3})([0-9]{3})([0-9]{2})$/, '$1.$2.$3-$4');
        });
        form.querySelectorAll('input[type=file]').forEach(input => {
            const feedback = form.querySelector('#' + input.id + '-name');
            input.addEventListener('change', () => {
                const file = input.files[0];
                input.setCustomValidity('');
                input.removeAttribute('aria-invalid');
                if (!file) { feedback.textContent = 'Nenhum arquivo selecionado.'; return; }
                feedback.textContent = file.name + ' (' + (file.size / 1024 / 1024).toFixed(2) + ' MB)';
                if (file.size === 0 || file.size > Number(input.dataset.maxFileBytes)) input.setCustomValidity('Envie um documento não vazio de até 5 MB.');
                else if (!/\.(pdf|jpe?g)$/i.test(file.name)) input.setCustomValidity('Envie um documento PDF ou JPG.');
                if (!input.checkValidity()) { input.setAttribute('aria-invalid', 'true'); input.reportValidity(); }
            });
        });
        if (cnpj.value && !legalName.value && cnpj.checkValidity()) lookup();
    };
    initialize(document);
    document.addEventListener('auth:profilechange', event => initialize(event.detail.root));
})();
