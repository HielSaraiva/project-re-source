(() => {
    const root = document.querySelector('[data-necessities-page]');
    if (!root) return;

    const dialog = document.querySelector('[data-necessity-dialog]');
    const form = dialog.querySelector('[data-necessity-form]');
    const title = dialog.querySelector('[data-dialog-title]');
    const submit = dialog.querySelector('[data-submit-necessity]');
    const error = dialog.querySelector('[data-form-error]');

    const clearError = () => {
        error.textContent = '';
        error.hidden = true;
    };

    const openCreate = () => {
        form.reset();
        form.dataset.mode = 'create';
        delete dialog.dataset.necessityId;
        title.textContent = 'Nova Necessidade';
        submit.textContent = 'Adicionar';
        clearError();
        dialog.showModal();
    };

    document.querySelectorAll('[data-open-create]').forEach((button) => {
        button.addEventListener('click', openCreate);
    });

    document.querySelectorAll('[data-open-edit]').forEach((button) => {
        button.addEventListener('click', () => {
            form.reset();
            form.dataset.mode = 'edit';
            dialog.dataset.necessityId = button.dataset.id;
            form.elements.category.value = button.dataset.category;
            form.elements.description.value = button.dataset.description;
            form.elements.quantity.value = button.dataset.quantity;
            form.elements.priority.value = button.dataset.priority;
            title.textContent = 'Editar Necessidade';
            submit.textContent = 'Salvar Alterações';
            clearError();
            dialog.showModal();
        });
    });

    dialog.querySelectorAll('[data-close-dialog]').forEach((button) => {
        button.addEventListener('click', () => dialog.close());
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearError();
        if (!form.reportValidity()) return;

        const creating = form.dataset.mode === 'create';
        const path = creating
            ? '/api/institution/necessities'
            : `/api/institution/necessities/${dialog.dataset.necessityId}`;
        const data = {
            category: form.elements.category.value,
            description: form.elements.description.value.trim(),
            quantity: Number(form.elements.quantity.value),
            priority: form.elements.priority.value,
        };
        const finishLoading = window.ResourceLoading.request(dialog, submit);
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 20000);

        try {
            await window.ResourceHttp.json(path, {
                method: creating ? 'POST' : 'PUT',
                data,
                signal: controller.signal,
            });
            window.location.reload();
        } catch (requestError) {
            error.textContent =
                requestError.name === 'AbortError' || requestError instanceof TypeError
                    ? 'Não foi possível confirmar o resultado. Atualize a página antes de tentar novamente.'
                    : requestError.message;
            error.hidden = false;
            error.focus();
        } finally {
            clearTimeout(timeout);
            finishLoading();
        }
    });
})();
