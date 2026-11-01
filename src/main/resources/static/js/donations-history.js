(() => {
    const newDialog = document.querySelector('#new-donation');
    const editDialog = document.querySelector('#edit-donation');
    const deleteDialog = document.querySelector('#delete-donation');
    if (!newDialog || typeof newDialog.showModal !== 'function') return;

    [newDialog, editDialog, deleteDialog].filter(Boolean).forEach((dialog) => {
        dialog
            .querySelectorAll('[data-close-modal]')
            .forEach((button) => button.addEventListener('click', () => dialog.close()));
        // Clique no fundo escurecido fecha o modal.
        dialog.addEventListener('click', (event) => {
            if (event.target === dialog) dialog.close();
        });
    });

    document
        .querySelectorAll('[data-open-new-donation]')
        .forEach((button) => button.addEventListener('click', () => newDialog.showModal()));

    if (deleteDialog) {
        const deleteForm = deleteDialog.querySelector('[data-delete-form]');
        const deleteBase = deleteForm.getAttribute('action');
        const deletePage = deleteDialog.dataset.page || '1';
        document.querySelectorAll('[data-delete-donation]').forEach((button) =>
            button.addEventListener('click', () => {
                const data = button.dataset;
                deleteForm.setAttribute(
                    'action',
                    `${deleteBase}/${data.id}/delete?page=${encodeURIComponent(deletePage)}`
                );
                deleteForm.querySelector('[data-delete-title]').textContent = data.title || '';
                deleteForm.querySelector('[data-delete-quantity]').textContent = data.quantity || '';
                deleteDialog.showModal();
            })
        );
    }

    if (!editDialog) return;
    const form = editDialog.querySelector('[data-edit-form]');
    const baseAction = form.getAttribute('action');
    const page = editDialog.dataset.page || '1';

    document.querySelectorAll('[data-edit-donation]').forEach((button) =>
        button.addEventListener('click', () => {
            const data = button.dataset;
            form.setAttribute('action', `${baseAction}/${data.id}/edit?page=${encodeURIComponent(page)}`);
            form.querySelector('[data-edit-title]').value = data.title || '';
            form.querySelector('[data-edit-description]').value = data.description || '';
            form.querySelector('[data-edit-category]').textContent = data.category || '';
            form.querySelector('[data-edit-icon]').setAttribute('src', data.icon || '');

            const quantity = form.querySelector('[data-edit-quantity]');
            const minimum = Number(data.min) || 1;
            quantity.min = String(minimum);
            quantity.value = data.quantity || '1';
            const hint = form.querySelector('[data-edit-min-hint]');
            hint.hidden = minimum <= 1;
            hint.textContent = `Mínimo de ${minimum} (já comprometidas ou recebidas).`;

            form.querySelector('[data-edit-other]').hidden = data.condition !== 'other';
            form.querySelectorAll('input[name="condition"]').forEach((radio) => {
                radio.checked = radio.value === data.condition;
            });
            editDialog.showModal();
        })
    );
})();
