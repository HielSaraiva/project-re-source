(() => {
    const root = document.querySelector('[data-inventory-page]');
    if (!root) return;
    const http = window.ResourceHttp;
    const dialog = document.querySelector('#inventory-form-dialog');
    const form = dialog.querySelector('form');
    const fields = form.querySelector('[data-inventory-fields]');
    const submit = form.querySelector('[data-save-inventory]');
    const error = form.querySelector('[data-form-error]');
    const loading = form.querySelector('[data-form-loading]');
    const recovery = form.querySelector('[data-form-recovery]');
    const newAttempt = form.querySelector('[data-new-attempt]');
    const reload = form.querySelector('[data-reload-form]');
    const linked = form.querySelector('[data-form-proposals]');
    const deleteDialog = document.querySelector('#inventory-delete-dialog');
    const deleteForm = deleteDialog.querySelector('form');
    const deleteSubmit = deleteForm.querySelector('[data-confirm-delete]');
    const deleteError = deleteForm.querySelector('[data-delete-error]');
    const historyDialog = document.querySelector('#inventory-proposals-dialog');
    const historyList = historyDialog.querySelector('[data-proposals-list]');
    const historyLoading = historyDialog.querySelector(
        '[data-proposals-loading]',
    );
    const historyError = historyDialog.querySelector('[data-proposals-error]');
    const historyRetry = historyDialog.querySelector('[data-retry-proposals]');
    const proposalStatus = historyDialog.querySelector(
        '[data-proposal-status]',
    );
    const historyPager = historyDialog.querySelector('[data-proposals-pager]');
    const previous = historyDialog.querySelector('[data-proposals-previous]');
    const next = historyDialog.querySelector('[data-proposals-next]');
    const storageKey = `resource:inventory-draft:${root.dataset.donorId}`;
    let draft = null;
    try {
        draft = JSON.parse(sessionStorage.getItem(storageKey));
    } catch {
        /* Storage can be unavailable. */
    }
    let mode = 'create',
        current = null,
        generation = 0,
        fetchController;
    let saving = false,
        deleting = false,
        opener,
        deleteOpener,
        historyOpener;
    let historyId,
        historyPage = 1,
        historyGeneration = 0,
        historyController;
    let submissionKey;

    const listUrl = (notice) => {
        const url = new URL(window.location.href);
        url.searchParams.delete('notice');
        if (notice) url.searchParams.set('notice', notice);
        return url.href;
    };
    document
        .querySelectorAll('[data-refresh-inventory], [data-delete-refresh]')
        .forEach((link) => (link.href = listUrl()));
    const showError = (message) => {
        error.textContent = message;
        error.hidden = false;
        error.focus();
    };
    const clearErrors = () => {
        error.hidden = true;
        recovery.hidden = true;
        newAttempt.hidden = true;
        form.querySelectorAll('[data-error-for]').forEach((node) => {
            node.hidden = true;
            node.textContent = '';
        });
        form.querySelectorAll('[aria-invalid]').forEach((node) =>
            node.removeAttribute('aria-invalid'),
        );
    };
    const fieldErrors = (requestError) => {
        showError(requestError.message);
        let first;
        Object.entries(requestError.body?.errors || {}).forEach(
            ([name, message]) => {
                const hint = [
                    ...form.querySelectorAll('[data-error-for]'),
                ].find((node) => node.dataset.errorFor === name);
                const control = form.elements.namedItem(name);
                if (hint && control) {
                    hint.textContent = message;
                    hint.hidden = false;
                    control.setAttribute('aria-invalid', 'true');
                    first ??= control;
                }
            },
        );
        first?.focus();
    };
    const readForm = () => ({
        category: form.elements.category.value,
        title: form.elements.title.value.trim(),
        quantity:
            form.elements.quantity.value === ''
                ? null
                : Number(form.elements.quantity.value),
        condition: form.elements.condition.value,
        description: form.elements.description.value.trim(),
    });
    const rememberDraft = () => {
        if (mode !== 'create') return;
        draft = { ...readForm(), submissionKey };
        try {
            sessionStorage.setItem(storageKey, JSON.stringify(draft));
        } catch {
            /* In-memory retry still works. */
        }
    };
    const clearDraft = () => {
        draft = null;
        try {
            sessionStorage.removeItem(storageKey);
        } catch {
            /* Optional persistence. */
        }
    };
    const fill = (values) => {
        for (const name of ['category', 'quantity', 'condition', 'description'])
            form.elements[name].value = values[name] ?? '';
        form.elements.title.value = values.title ?? values.itemName ?? '';
    };
    const context = (details) => {
        const minimum = Math.max(1, details.receivedQuantity);
        form.elements.quantity.min = String(minimum);
        const hint = form.querySelector('[data-quantity-hint]');
        hint.hidden = details.receivedQuantity === 0;
        const completing =
            details.receivedQuantity > 0 &&
            Number(form.elements.quantity.value) === details.receivedQuantity;
        hint.textContent = `${details.receivedQuantity} unidades já recebidas. A quantidade cadastrada não pode ficar abaixo desse total.${completing ? ' Ao salvar esse total, a doação será concluída e não poderá mais ser editada.' : ''}`;
    };
    const recover = (details) => {
        recovery.hidden = false;
        reload.hidden = !details?.editable;
        linked.hidden = !details?.proposalCount;
    };
    const authMessage = (requestError) =>
        [401, 403].includes(requestError.status)
            ? 'Sua sessão expirou ou sua conta não tem permissão. Atualize a página para entrar novamente.'
            : null;

    const loadDetails = async (preserve = false) => {
        fetchController?.abort();
        const controller = new AbortController();
        fetchController = controller;
        const session = generation,
            id = dialog.dataset.id;
        fields.disabled = true;
        submit.disabled = true;
        loading.hidden = false;
        const timeout = setTimeout(() => controller.abort(), 10000);
        try {
            const details = await http.json(`/api/donor/inventory/${id}`, {
                cache: 'no-store',
                signal: controller.signal,
            });
            if (
                generation !== session ||
                controller !== fetchController ||
                !dialog.open
            )
                return;
            if (!preserve) {
                current = details;
                fill(details);
                context(details);
                clearErrors();
            }
            if (!details.editable) {
                recover(details);
                showError(
                    details.editRestriction ||
                        'Esta doação não pode mais ser editada. Atualize a lista.',
                );
            } else if (preserve) {
                recover(details);
                showError(
                    'Esta doação foi alterada enquanto você editava. Seus campos foram preservados. Recarregar dados substituirá o formulário pela versão atual para você revisar.',
                );
            } else {
                fields.disabled = false;
                submit.disabled = false;
                form.elements.title.focus();
            }
        } catch (requestError) {
            if (
                generation !== session ||
                controller !== fetchController ||
                !dialog.open
            )
                return;
            recover(null);
            reload.hidden = [401, 403, 404].includes(requestError.status);
            showError(
                authMessage(requestError) ||
                    (requestError.status === 404
                        ? 'Esta doação foi excluída ou não está mais disponível. Atualize a lista.'
                        : 'Não foi possível carregar os dados. Seus campos foram preservados; tente recarregar.'),
            );
        } finally {
            clearTimeout(timeout);
            if (generation === session && controller === fetchController)
                loading.hidden = true;
        }
    };
    const openForm = (button, id) => {
        generation++;
        fetchController?.abort();
        opener = button;
        current = null;
        form.reset();
        clearErrors();
        mode = id === undefined ? 'create' : 'edit';
        dialog.dataset.id = id || '';
        const editing = mode === 'edit';
        form.querySelector('#inventory-form-title').textContent = editing
            ? 'Editar doação'
            : 'Nova doação';
        form.querySelector('[data-dialog-status]').textContent = editing
            ? `Doação #${id}`
            : 'Novo cadastro';
        form.querySelector('[data-dialog-icon]').src = http.url(
            editing
                ? '/images/shared/icons/clipboard-pen.svg'
                : '/images/donor/donation-status/gift.svg',
        );
        form.querySelector('[data-category-hint]').hidden = !editing;
        form.querySelector('[data-quantity-hint]').hidden = true;
        form.elements.category.disabled = editing;
        form.elements.quantity.min = '1';
        submit.textContent = editing ? 'Salvar alterações' : 'Cadastrar doação';
        fields.disabled = editing;
        submit.disabled = editing;
        loading.hidden = true;
        if (!editing) {
            submissionKey = draft?.submissionKey || crypto.randomUUID();
            if (draft) fill(draft);
        }
        dialog.showModal();
        if (editing) loadDetails();
        else form.elements.category.focus();
    };
    document
        .querySelectorAll('[data-open-new-donation]')
        .forEach((button) =>
            button.addEventListener('click', () => openForm(button)),
        );
    document
        .querySelectorAll('[data-edit-donation]')
        .forEach((button) =>
            button.addEventListener('click', () =>
                openForm(button, button.dataset.id),
            ),
        );
    form.querySelector('[data-close-inventory]').addEventListener('click', () =>
        dialog.close(),
    );
    dialog.addEventListener('cancel', (event) => {
        if (saving) event.preventDefault();
    });
    dialog.addEventListener('close', () => {
        rememberDraft();
        generation++;
        fetchController?.abort();
        opener?.focus();
    });
    reload.addEventListener('click', () => loadDetails());
    newAttempt.addEventListener('click', () => {
        // Only offered after a definitive conflict, never for an unknown network outcome.
        clearDraft();
        submissionKey = crypto.randomUUID();
        form.reset();
        clearErrors();
        fields.disabled = false;
        submit.disabled = false;
        form.elements.category.focus();
    });
    form.addEventListener('input', () => {
        rememberDraft();
        if (mode === 'edit' && current) context(current);
    });
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (saving || fields.disabled) return;
        form.elements.title.value = form.elements.title.value.trim();
        if (!form.reportValidity()) return;
        clearErrors();
        rememberDraft();
        saving = true;
        const editing = mode === 'edit';
        const data = readForm();
        if (editing) {
            delete data.category;
            data.version = current.version;
        } else data.submissionKey = submissionKey;
        const finish = window.ResourceLoading.request(dialog, submit);
        fields.disabled = true;
        let blocked = false;
        try {
            await http.json(
                `/api/donor/inventory${editing ? `/${dialog.dataset.id}` : ''}`,
                {
                    method: editing ? 'PUT' : 'POST',
                    data,
                    signal: AbortSignal.timeout(20000),
                },
            );
            if (!editing) clearDraft();
            window.location.assign(listUrl(editing ? 'updated' : 'created'));
        } catch (requestError) {
            if (editing && [404, 409].includes(requestError.status)) {
                blocked = true;
                await loadDetails(true);
            } else if (requestError.status === 400) {
                fields.disabled = false;
                fieldErrors(requestError);
            } else {
                recovery.hidden = false;
                newAttempt.hidden = editing || requestError.status !== 409;
                reload.hidden = true;
                linked.hidden = true;
                blocked =
                    editing || [401, 403, 409].includes(requestError.status);
                showError(
                    authMessage(requestError) ||
                        (requestError.status === 409
                            ? `${requestError.message}${editing ? '' : ' Confira a lista: a tentativa anterior pode já estar cadastrada. Para cadastrar outra doação, use “Iniciar outro cadastro”.'}`
                            : editing
                              ? 'Não foi possível confirmar a alteração. Seus campos foram preservados. Atualize a lista antes de tentar novamente.'
                              : 'Não foi possível confirmar o cadastro. Seus campos foram preservados; você pode tentar novamente. A mesma tentativa não criará outra doação.'),
                );
            }
        } finally {
            finish();
            fields.disabled = blocked;
            submit.disabled = blocked;
            form.elements.category.disabled = editing;
            saving = false;
        }
    });

    document.querySelectorAll('[data-delete-donation]').forEach((button) =>
        button.addEventListener('click', () => {
            deleteOpener = button;
            deleteDialog.dataset.id = button.dataset.id;
            deleteDialog.dataset.version = button.dataset.version;
            deleteForm.querySelector('[data-dialog-status]').textContent =
                `Doação #${button.dataset.id}`;
            deleteForm.querySelector('[data-delete-title]').textContent =
                button.dataset.title;
            deleteForm.querySelector('[data-delete-quantity]').textContent =
                button.dataset.quantity;
            deleteError.hidden = true;
            deleteSubmit.disabled = false;
            deleteForm.querySelector('[data-delete-history]').hidden =
                button.dataset.hasProposals !== 'true';
            deleteForm.querySelector('[data-delete-refresh]').hidden = true;
            deleteForm.querySelector('[data-delete-proposals]').hidden = true;
            deleteDialog.showModal();
            deleteForm.querySelector('[data-close-delete]').focus();
        }),
    );
    deleteForm
        .querySelector('[data-close-delete]')
        .addEventListener('click', () => deleteDialog.close());
    deleteDialog.addEventListener('cancel', (event) => {
        if (deleting) event.preventDefault();
    });
    deleteDialog.addEventListener('close', () => deleteOpener?.focus());
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
            await http.json(
                `/api/donor/inventory/${deleteDialog.dataset.id}?version=${deleteDialog.dataset.version}`,
                {
                    method: 'DELETE',
                    signal: AbortSignal.timeout(20000),
                },
            );
            window.location.assign(listUrl('deleted'));
        } catch (requestError) {
            failed = true;
            deleteError.textContent =
                authMessage(requestError) ||
                ([404, 409].includes(requestError.status)
                    ? requestError.message
                    : 'Não foi possível confirmar a exclusão. Atualize a lista antes de tentar novamente.');
            deleteError.hidden = false;
            deleteError.focus();
            deleteForm.querySelector('[data-delete-refresh]').hidden = false;
            if (requestError.status === 409) {
                try {
                    const latest = await http.json(
                        `/api/donor/inventory/${deleteDialog.dataset.id}`,
                        { cache: 'no-store' },
                    );
                    deleteForm.querySelector('[data-delete-proposals]').hidden =
                        latest.proposalCount === 0;
                    if (latest.deleteRestriction)
                        deleteError.textContent = latest.deleteRestriction;
                } catch {
                    /* Keep the original failure and recovery link. */
                }
            }
        } finally {
            finish();
            deleteSubmit.disabled = failed;
            deleting = false;
        }
    });

    const element = (tag, className, text) => {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined) node.textContent = text;
        return node;
    };
    const renderProposal = (proposal) => {
        const article = element('article', 'inventory-proposal');
        const heading = element('div', 'inventory-proposal__heading');
        heading.append(element('strong', '', proposal.protocol));
        const style =
            proposal.status === 'completed'
                ? 'completed'
                : ['cancelled', 'rejected'].includes(proposal.status)
                  ? 'cancelled'
                  : proposal.status === 'in_transit'
                    ? 'shipped'
                    : 'waiting';
        heading.append(
            element(
                'span',
                `donation-status donation-status--${style}`,
                proposal.statusLabel,
            ),
        );
        article.append(heading, element('p', '', proposal.institution));
        const summary = element('dl');
        for (const [label, value] of [
            ['Quantidade', proposal.quantity],
            [
                'Proposta enviada em',
                new Date(proposal.createdAt).toLocaleDateString('pt-BR', {
                    timeZone: 'America/Fortaleza',
                }),
            ],
        ]) {
            const row = element('div');
            row.append(element('dt', '', label), element('dd', '', value));
            summary.append(row);
        }
        const link = element(
            'a',
            'inventory-inline-action',
            'Abrir protocolo →',
        );
        link.href = http.url(
            `/donor/donation/status?protocol=${encodeURIComponent(proposal.protocol)}`,
        );
        link.setAttribute('aria-label', `Abrir protocolo ${proposal.protocol}`);
        article.append(summary, link);
        return article;
    };
    const loadProposals = async (page) => {
        historyController?.abort();
        const controller = new AbortController();
        historyController = controller;
        const session = historyGeneration;
        historyPage = page;
        historyLoading.hidden = false;
        historyError.hidden = true;
        historyRetry.hidden = true;
        historyPager.hidden = true;
        historyList.replaceChildren();
        const timeout = setTimeout(() => controller.abort(), 10000);
        try {
            const result = await http.json(
                `/api/donor/inventory/${historyId}/proposals?page=${page}${proposalStatus.value ? `&status=${encodeURIComponent(proposalStatus.value)}` : ''}`,
                { cache: 'no-store', signal: controller.signal },
            );
            if (
                historyGeneration !== session ||
                historyController !== controller ||
                !historyDialog.open
            )
                return;
            historyPage = result.page;
            result.content.forEach((proposal) =>
                historyList.append(renderProposal(proposal)),
            );
            if (!result.content.length)
                historyList.append(
                    element(
                        'p',
                        'inventory-field-hint',
                        proposalStatus.value
                            ? 'Nenhuma proposta encontrada com esse status.'
                            : 'Esta doação ainda não possui propostas.',
                    ),
                );
            historyPager.hidden = result.totalPages <= 1;
            previous.disabled = result.page <= 1;
            next.disabled = result.page >= result.totalPages;
            historyDialog.querySelector('[data-proposals-page]').textContent =
                `Página ${result.page} de ${result.totalPages}`;
        } catch (requestError) {
            if (
                historyGeneration !== session ||
                historyController !== controller ||
                !historyDialog.open
            )
                return;
            historyError.textContent =
                authMessage(requestError) ||
                (requestError.status === 404
                    ? 'Doação não encontrada. Atualize a lista.'
                    : 'Não foi possível carregar as propostas. Tente novamente.');
            historyError.hidden = false;
            historyRetry.hidden = [401, 403, 404].includes(requestError.status);
            historyError.focus();
        } finally {
            clearTimeout(timeout);
            if (
                historyGeneration === session &&
                historyController === controller
            )
                historyLoading.hidden = true;
        }
    };
    const openHistory = (id, title, button) => {
        historyGeneration++;
        historyId = id;
        historyOpener = button;
        proposalStatus.value = '';
        historyDialog.querySelector('[data-dialog-status]').textContent =
            `Doação #${id}`;
        historyDialog.querySelector(
            '[data-proposals-description]',
        ).textContent =
            `${title ? title + '. ' : ''}Consulte todas as propostas e seus protocolos, inclusive recusadas e canceladas.`;
        historyDialog.showModal();
        loadProposals(1);
    };
    document
        .querySelectorAll('[data-open-proposals]')
        .forEach((button) =>
            button.addEventListener('click', () =>
                openHistory(button.dataset.id, button.dataset.title, button),
            ),
        );
    linked.addEventListener('click', () => {
        const id = dialog.dataset.id;
        dialog.close();
        openHistory(id, '', opener);
    });
    deleteForm
        .querySelector('[data-delete-proposals]')
        .addEventListener('click', () => {
            const id = deleteDialog.dataset.id;
            deleteDialog.close();
            openHistory(id, '', deleteOpener);
        });
    historyDialog
        .querySelector('[data-close-proposals]')
        .addEventListener('click', () => historyDialog.close());
    historyDialog.addEventListener('close', () => {
        historyGeneration++;
        historyController?.abort();
        historyOpener?.focus();
    });
    proposalStatus.addEventListener('change', () => loadProposals(1));
    previous.addEventListener('click', () => loadProposals(historyPage - 1));
    next.addEventListener('click', () => loadProposals(historyPage + 1));
    historyRetry.addEventListener('click', () => loadProposals(historyPage));
    root.querySelectorAll('.donations-filters select').forEach((control) =>
        control.addEventListener('change', () => {
            if (control.name === 'view')
                control.form.elements.status.value = '';
            control.form.requestSubmit();
        }),
    );
    const success = root.querySelector('[data-operation-success]');
    if (success) {
        success.focus();
        window.history.replaceState({}, '', listUrl());
    }
})();
