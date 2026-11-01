(() => {
    const root = document.querySelector('[data-necessities-page]');
    if (!root) return;
    const dialog = document.querySelector('[data-necessity-dialog]');
    const form = dialog.querySelector('form');
    const fields = form.querySelector('[data-fields]');
    const submit = form.querySelector('[data-submit-necessity]');
    const error = form.querySelector('[data-form-error]');
    const recovery = form.querySelector('[data-recovery]');
    const reload = form.querySelector('[data-reload-details]');
    const proposals = form.querySelector('[data-linked-proposals]');
    const loading = form.querySelector('[data-details-loading]');
    const deleteDialog = document.querySelector('[data-delete-dialog]');
    const deleteForm = deleteDialog.querySelector('form');
    const deleteSubmit = deleteForm.querySelector('[data-confirm-delete]');
    const deleteError = deleteForm.querySelector('[data-delete-error]');
    let session = 0;
    let detailsRequest;
    let current = null;
    let saving = false;
    let deleting = false;
    let opener;

    document
        .querySelectorAll('[data-refresh-list], [data-delete-refresh]')
        .forEach((link) => {
            const url = new URL(window.location.href);
            url.searchParams.delete('notice');
            link.href = url.href;
        });
    const returnUrl = (notice) => {
        const url = new URL(window.location.href);
        url.searchParams.set('notice', notice);
        return url.href;
    };
    const showError = (message) => {
        error.textContent = message;
        error.hidden = false;
        error.focus();
    };
    const resetError = () => {
        error.hidden = true;
        error.textContent = '';
        recovery.hidden = true;
        proposals.hidden = true;
    };
    const recoveryLinks = (details) => {
        recovery.hidden = false;
        reload.hidden = !details?.editable;
        proposals.hidden = !details?.hasProposals;
        if (details?.hasProposals)
            proposals.href = window.ResourceHttp.url(
                `/institution/donations?necessityId=${details.id}`,
            );
    };
    const fill = (details) => {
        form.elements.category.value = details.category;
        form.elements.description.value =
            details.description || details.itemName;
        form.elements.quantity.value = details.quantity;
        form.elements.priority.value = details.priority;
    };
    const loadDetails = async ({ preserve = false } = {}) => {
        detailsRequest?.abort();
        const controller = new AbortController();
        detailsRequest = controller;
        const thisSession = session;
        const id = dialog.dataset.necessityId;
        fields.disabled = true;
        submit.disabled = true;
        loading.hidden = false;
        const timeout = setTimeout(() => controller.abort(), 10000);
        try {
            const latest = await window.ResourceHttp.json(
                `/api/institution/necessities/${id}`,
                {
                    cache: 'no-store',
                    signal: controller.signal,
                },
            );
            // Neither successful responses nor errors may update a different modal session.
            if (
                session !== thisSession ||
                detailsRequest !== controller ||
                !dialog.open
            )
                return;
            if (!preserve) {
                current = latest;
                fill(latest);
                resetError();
            }
            if (!latest.editable) {
                recoveryLinks(latest);
                showError(
                    latest.restrictionReason ||
                        'Esta necessidade não pode mais ser editada. Atualize a lista.',
                );
            } else if (preserve) {
                // Keep the original version until the user explicitly reloads; never silently
                // overwrite someone else's edit or discard the values currently typed.
                recoveryLinks(latest);
                showError(
                    'Os dados mudaram enquanto você editava. Seus campos foram preservados. Recarregue os dados para revisar a versão atual antes de salvar.',
                );
            } else {
                fields.disabled = false;
                submit.disabled = false;
            }
        } catch (requestError) {
            if (
                session !== thisSession ||
                detailsRequest !== controller ||
                !dialog.open
            )
                return;
            recoveryLinks(null);
            reload.hidden = requestError.status === 404;
            showError(
                requestError.status === 404
                    ? 'Esta necessidade foi excluída ou não está mais disponível. Atualize a lista.'
                    : 'Não foi possível carregar os dados atualizados. Seus campos foram preservados; tente recarregar os dados.',
            );
        } finally {
            clearTimeout(timeout);
            if (session === thisSession && detailsRequest === controller)
                loading.hidden = true;
        }
    };
    const open = (button, id) => {
        session++;
        detailsRequest?.abort();
        current = null;
        opener = button;
        form.reset();
        form.elements.quantity.setCustomValidity('');
        resetError();
        const editing = id !== undefined;
        form.dataset.mode = editing ? 'edit' : 'create';
        dialog.dataset.necessityId = id || '';
        form.querySelector('#necessity-dialog-title').textContent = editing
            ? 'Editar necessidade'
            : 'Nova necessidade';
        submit.textContent = editing
            ? 'Salvar alterações'
            : 'Cadastrar necessidade';
        fields.disabled = editing;
        submit.disabled = editing;
        dialog.showModal();
        if (editing) loadDetails();
        else form.elements.category.focus();
    };
    document
        .querySelectorAll('[data-open-create]')
        .forEach((button) =>
            button.addEventListener('click', () => open(button)),
        );
    document
        .querySelectorAll('[data-open-edit]')
        .forEach((button) =>
            button.addEventListener('click', () =>
                open(button, button.dataset.id),
            ),
        );
    form.querySelector('[data-close-dialog]').addEventListener('click', () =>
        dialog.close(),
    );
    dialog.addEventListener('cancel', (event) => {
        if (saving) event.preventDefault();
    });
    dialog.addEventListener('close', () => {
        session++;
        detailsRequest?.abort();
        opener?.focus();
    });
    reload.addEventListener('click', () => loadDetails());
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (saving || fields.disabled) return;
        form.elements.description.value =
            form.elements.description.value.trim();
        if (!form.reportValidity()) return;
        saving = true;
        const editing = form.dataset.mode === 'edit';
        const id = dialog.dataset.necessityId;
        const payload = {
            category: form.elements.category.value,
            description: form.elements.description.value,
            quantity: Number(form.elements.quantity.value),
            priority: form.elements.priority.value,
            version: editing ? current.version : null,
        };
        const finish = window.ResourceLoading.request(dialog, submit);
        fields.disabled = true;
        let conflict = false;
        try {
            await window.ResourceHttp.json(
                `/api/institution/necessities${editing ? `/${id}` : ''}`,
                {
                    method: editing ? 'PUT' : 'POST',
                    data: payload,
                    signal: AbortSignal.timeout(20000),
                },
            );
            window.location.assign(returnUrl(editing ? 'updated' : 'created'));
        } catch (requestError) {
            if (editing && [404, 409].includes(requestError.status)) {
                conflict = true;
                await loadDetails({ preserve: true });
            } else {
                if (requestError.status !== 400) {
                    conflict = true;
                    recoveryLinks(null);
                }
                showError(
                    requestError.status === 400
                        ? 'Confira o nome do item (até 150 caracteres), a categoria, a prioridade e a quantidade inteira entre 1 e 2.147.483.647.'
                        : requestError.status === 403 ||
                            requestError.status === 401
                          ? 'Sua sessão expirou ou sua conta não tem permissão. Atualize a página para entrar novamente.'
                          : 'Não foi possível confirmar o resultado. Atualize a lista antes de tentar novamente para evitar duplicidade.',
                );
            }
        } finally {
            finish();
            fields.disabled = conflict;
            submit.disabled = conflict;
            saving = false;
        }
    });
    document.querySelectorAll('[data-open-delete]').forEach((button) => {
        button.addEventListener('click', () => {
            opener = button;
            deleteDialog.dataset.necessityId = button.dataset.id;
            deleteDialog.dataset.version = button.dataset.version;
            deleteForm.querySelector('[data-delete-description]').textContent =
                button.dataset.description;
            deleteForm.querySelector('[data-delete-id]').textContent =
                `#${button.dataset.id}`;
            deleteForm.querySelector('[data-delete-history]').hidden =
                button.dataset.hasProposals !== 'true';
            deleteForm.querySelector('[data-delete-refresh]').hidden = true;
            deleteError.hidden = true;
            deleteSubmit.disabled = false;
            deleteDialog.showModal();
            deleteForm.querySelector('[data-close-delete]').focus();
        });
    });
    deleteForm
        .querySelector('[data-close-delete]')
        .addEventListener('click', () => deleteDialog.close());
    deleteDialog.addEventListener('cancel', (event) => {
        if (deleting) event.preventDefault();
    });
    deleteDialog.addEventListener('close', () => opener?.focus());
    deleteForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (deleting) return;
        deleting = true;
        const finish = window.ResourceLoading.request(
            deleteDialog,
            deleteSubmit,
        );
        let failed = false;
        try {
            await window.ResourceHttp.json(
                `/api/institution/necessities/${deleteDialog.dataset.necessityId}?version=${deleteDialog.dataset.version}`,
                {
                    method: 'DELETE',
                    signal: AbortSignal.timeout(20000),
                },
            );
            window.location.assign(returnUrl('deleted'));
        } catch (requestError) {
            failed = true;
            deleteError.textContent =
                requestError.status === 409 || requestError.status === 404
                    ? requestError.message
                    : 'Não foi possível confirmar a exclusão. Atualize a lista antes de tentar novamente.';
            deleteError.hidden = false;
            deleteError.focus();
            deleteForm.querySelector('[data-delete-refresh]').hidden = false;
        } finally {
            finish();
            deleteSubmit.disabled = failed;
            deleting = false;
        }
    });
    // Explicit search avoids losing focus mid-sentence. Select changes are intentional navigation.
    root.querySelectorAll('.necessities-filters select').forEach((field) =>
        field.addEventListener('change', () => field.form.requestSubmit()),
    );
    const success = root.querySelector('[data-operation-success]');
    if (success) {
        success.focus();
        const url = new URL(window.location.href);
        url.searchParams.delete('notice');
        window.history.replaceState({}, '', url);
    }
})();
