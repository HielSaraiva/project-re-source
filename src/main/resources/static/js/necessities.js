(() => {
    const root = document.querySelector('[data-necessities-page]');
    if (!root) return;

    const dialog = document.querySelector('[data-necessity-dialog]');
    const form = dialog.querySelector('[data-necessity-form]');
    const title = dialog.querySelector('[data-dialog-title]');
    const submit = dialog.querySelector('[data-submit-necessity]');
    const error = dialog.querySelector('[data-form-error]');
    const quantityField = form.elements.quantity;
    const quantityHint = dialog.querySelector('[data-quantity-hint]');
    const categoryField = form.elements.category;
    const categoryHint = dialog.querySelector('[data-category-hint]');
    const deleteDialog = document.querySelector('[data-delete-dialog]');
    const deleteForm = deleteDialog.querySelector('[data-delete-form]');
    const deleteError = deleteForm.querySelector('[data-delete-error]');
    const deleteSubmit = deleteForm.querySelector('[data-confirm-delete]');
    const fulfillmentDialog = document.querySelector('[data-fulfill-confirm]');
    let currentDetails = null;
    let fulfillmentConfirmed = false;
    let searchTimer;

    const clearError = () => {
        error.textContent = '';
        error.hidden = true;
    };

    const quantityPhrase = (count, singular, plural) =>
        `${count} ${count === 1 ? singular : plural}`;
    const quantityBreakdown = (received, reserved) =>
        `${quantityPhrase(received, 'unidade recebida', 'unidades recebidas')} e ${quantityPhrase(reserved, 'unidade reservada', 'unidades reservadas')}`;

    const showError = (message) => {
        error.textContent = message;
        error.hidden = false;
        error.focus();
    };

    const setQuantityContext = (details) => {
        currentDetails = details;
        const minimum = Number(details.minimumQuantity);
        quantityField.min = String(Math.max(1, minimum));
        quantityHint.textContent =
            minimum > 0
                ? `Mínimo permitido: ${quantityPhrase(minimum, 'unidade', 'unidades')} (${quantityBreakdown(details.receivedQuantity, details.reservedQuantity)}).`
                : 'Informe a quantidade total desejada.';
        quantityField.dataset.minimum = String(minimum);
        validateQuantity();
    };

    const setCategoryContext = (details, preserveSelection = true) => {
        const selectedCategory = categoryField.value;
        categoryField.disabled = Boolean(details.hasProposals);
        categoryHint.hidden = !details.hasProposals;
        if (details.hasProposals) categoryField.value = details.category;
        else if (preserveSelection && selectedCategory) categoryField.value = selectedCategory;
    };

    const refreshEditDetails = async (preserveSelection = true) => {
        const id = dialog.dataset.necessityId;
        const latest = await window.ResourceHttp.json(`/api/institution/necessities/${id}`, {
            cache: 'no-store',
            signal: AbortSignal.timeout(10000),
        });
        setQuantityContext(latest);
        setCategoryContext(latest, preserveSelection);
        return latest;
    };

    const openCreate = () => {
        form.reset();
        form.dataset.mode = 'create';
        delete dialog.dataset.necessityId;
        title.textContent = 'Nova Necessidade';
        submit.textContent = 'Adicionar';
        submit.disabled = false;
        categoryField.disabled = false;
        categoryHint.hidden = true;
        quantityField.min = '1';
        quantityField.dataset.minimum = '0';
        quantityHint.textContent = 'Informe a quantidade total desejada.';
        currentDetails = null;
        fulfillmentConfirmed = false;
        clearError();
        dialog.showModal();
        categoryField.focus();
    };

    document.querySelectorAll('[data-open-create]').forEach((button) => {
        button.addEventListener('click', openCreate);
    });

    document.querySelectorAll('[data-open-edit]').forEach((button) => {
        button.addEventListener('click', async () => {
            form.reset();
            form.dataset.mode = 'edit';
            dialog.dataset.necessityId = button.dataset.id;
            categoryField.value = button.dataset.category;
            form.elements.description.value = button.dataset.description;
            quantityField.value = button.dataset.quantity;
            form.elements.priority.value = button.dataset.priority;
            title.textContent = 'Editar Necessidade';
            submit.textContent = 'Salvar Alterações';
            submit.disabled = false;
            setQuantityContext({
                category: button.dataset.category,
                quantity: Number(button.dataset.quantity),
                minimumQuantity: Number(button.dataset.minimum),
                receivedQuantity: Number(button.dataset.received),
                reservedQuantity: Number(button.dataset.reserved),
                hasProposals: button.dataset.hasProposals === 'true',
            });
            setCategoryContext(
                {
                    category: button.dataset.category,
                    hasProposals: button.dataset.hasProposals === 'true',
                },
                false,
            );
            fulfillmentConfirmed = false;
            clearError();
            dialog.showModal();
            try {
                await refreshEditDetails(false);
            } catch (requestError) {
                showError(
                    requestError.message ||
                        'Não foi possível atualizar as informações da necessidade. Confira sua conexão e tente novamente.',
                );
            }
        });
    });

    dialog.querySelectorAll('[data-close-dialog]').forEach((button) => {
        button.addEventListener('click', () => dialog.close());
    });

    dialog.querySelector('[data-create-another]').addEventListener('click', () => {
        dialog.close();
        openCreate();
    });

    document.querySelectorAll('[data-open-delete]').forEach((button) => {
        button.addEventListener('click', () => {
            deleteDialog.dataset.necessityId = button.dataset.id;
            deleteDialog.querySelector('[data-delete-description]').textContent =
                button.dataset.description;
            deleteError.textContent = '';
            deleteError.hidden = true;
            deleteSubmit.hidden = false;
            deleteSubmit.disabled = false;
            deleteDialog.showModal();
        });
    });

    deleteDialog.querySelectorAll('[data-close-delete]').forEach((button) => {
        button.addEventListener('click', () => deleteDialog.close());
    });

    deleteForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        deleteError.textContent = '';
        deleteError.hidden = true;
        const id = deleteDialog.dataset.necessityId;
        const finishLoading = window.ResourceLoading.request(deleteDialog, deleteSubmit);
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 20000);

        try {
            await window.ResourceHttp.json(`/api/institution/necessities/${id}`, {
                method: 'DELETE',
                signal: controller.signal,
            });
            window.location.assign(
                window.ResourceHttp.url('/institution/necessities?notice=deleted'),
            );
        } catch (requestError) {
            deleteError.textContent =
                requestError.name === 'AbortError' || requestError instanceof TypeError
                    ? 'Não foi possível confirmar a exclusão. Atualize a página antes de tentar novamente.'
                    : requestError.message;
            deleteError.hidden = false;
            deleteError.focus();
        } finally {
            clearTimeout(timeout);
            finishLoading();
        }
    });

    const validateQuantity = () => {
        if (quantityField.value === '') {
            quantityField.setCustomValidity('');
            return false;
        }
        const value = quantityField.valueAsNumber;
        if (!Number.isInteger(value)) {
            quantityField.setCustomValidity('Informe uma quantidade inteira.');
            return false;
        }
        const minimum = Number(quantityField.dataset.minimum || 0);
        if (form.dataset.mode === 'edit' && value < minimum) {
            const received = currentDetails?.receivedQuantity ?? 0;
            const reserved = currentDetails?.reservedQuantity ?? 0;
            quantityField.setCustomValidity(
                `O mínimo é ${quantityPhrase(minimum, 'unidade', 'unidades')}: ${quantityBreakdown(received, reserved)}.`,
            );
            return false;
        }
        quantityField.setCustomValidity('');
        return true;
    };

    quantityField.addEventListener('input', () => {
        validateQuantity();
        if (!error.hidden) clearError();
    });

    const wouldFulfillNeed = (requestedQuantity) =>
        form.dataset.mode === 'edit' &&
        currentDetails !== null &&
        requestedQuantity < Number(currentDetails.quantity) &&
        Number(currentDetails.receivedQuantity) >= requestedQuantity &&
        requestedQuantity >= Number(currentDetails.minimumQuantity);

    const completeUpdate = async () => {
        const creating = form.dataset.mode === 'create';
        const path = creating
            ? '/api/institution/necessities'
            : `/api/institution/necessities/${dialog.dataset.necessityId}`;
        const requestedQuantity = Number(quantityField.value);
        const data = {
            category: categoryField.value,
            description: form.elements.description.value.trim(),
            quantity: requestedQuantity,
            priority: form.elements.priority.value,
            confirmFulfillment: fulfillmentConfirmed,
        };
        const finishLoading = window.ResourceLoading.request(dialog, submit);
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 20000);

        try {
            const result = await window.ResourceHttp.json(path, {
                method: creating ? 'POST' : 'PUT',
                data,
                signal: controller.signal,
            });
            const notice =
                creating ? 'created' : result.status === 'fulfilled' ? 'updated-fulfilled' : 'updated';
            window.location.assign(
                window.ResourceHttp.url(`/institution/necessities?notice=${notice}`),
            );
        } catch (requestError) {
            if (requestError.status === 409 && !creating) {
                try {
                    const previousMinimum = Number(quantityField.dataset.minimum || 0);
                    const previouslyHadProposals = currentDetails?.hasProposals ?? false;
                    const latest = await refreshEditDetails();
                    const requestedQuantity = Number(quantityField.value);
                    if (
                        requestedQuantity < Number(latest.quantity) &&
                        requestedQuantity >= Number(latest.minimumQuantity) &&
                        Number(latest.receivedQuantity) >= requestedQuantity
                    ) {
                        fulfillmentConfirmed = false;
                        clearError();
                        fulfillmentDialog.querySelector('[data-fulfill-message]').textContent =
                            `A necessidade foi atualizada enquanto você editava. A quantidade informada (${requestedQuantity}) fará com que ela seja marcada como atendida, pois ${latest.receivedQuantity} unidades já foram recebidas.`;
                        fulfillmentDialog.showModal();
                        return;
                    }
                    const minimumChanged =
                        Number(latest.minimumQuantity) > previousMinimum ||
                        latest.hasProposals !== previouslyHadProposals;
                    const context =
                        `O mínimo agora é ${quantityPhrase(latest.minimumQuantity, 'unidade', 'unidades')}: ${quantityBreakdown(latest.receivedQuantity, latest.reservedQuantity)}.`;
                    showError(
                        `${minimumChanged ? 'A necessidade foi atualizada por outra operação. ' : ''}${context} Seus dados foram preservados; ajuste a quantidade e tente salvar novamente.`,
                    );
                    fulfillmentConfirmed = false;
                } catch (refreshError) {
                    showError(
                        `${requestError.message} Não foi possível atualizar o mínimo atual. Seus dados foram preservados; tente novamente.`,
                    );
                }
            } else {
                showError(
                    requestError.name === 'AbortError' || requestError instanceof TypeError
                        ? 'Não foi possível confirmar o resultado. Atualize a página antes de tentar novamente.'
                        : requestError.message,
                );
            }
        } finally {
            clearTimeout(timeout);
            finishLoading();
        }
    };

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearError();
        validateQuantity();
        if (!form.reportValidity()) return;

        const requestedQuantity = Number(quantityField.value);
        if (!fulfillmentConfirmed && wouldFulfillNeed(requestedQuantity)) {
            const count = currentDetails.receivedQuantity;
            fulfillmentDialog.querySelector('[data-fulfill-message]').textContent =
                `Você está reduzindo a quantidade para ${requestedQuantity} e já foram recebidas ${count} unidades. Esta alteração marcará a necessidade como atendida.`;
            fulfillmentDialog.showModal();
            return;
        }
        await completeUpdate();
    });

    fulfillmentDialog.querySelectorAll('[data-cancel-fulfill]').forEach((button) => {
        button.addEventListener('click', () => {
            fulfillmentDialog.close();
            fulfillmentConfirmed = false;
            quantityField.focus();
        });
    });

    fulfillmentDialog.querySelector('[data-confirm-fulfill]').addEventListener('click', () => {
        fulfillmentDialog.close();
        fulfillmentConfirmed = true;
        form.requestSubmit();
    });

    const success = root.querySelector('[data-operation-success]');
    if (success) {
        success.focus();
        const url = new URL(window.location.href);
        url.searchParams.delete('notice');
        window.history.replaceState({}, '', url);
    }

    const filters = root.querySelector('.necessities-filters');
    filters.querySelectorAll('select').forEach((field) => {
        field.addEventListener('change', () => {
            clearTimeout(searchTimer);
            filters.requestSubmit();
        });
    });
    const search = filters.elements.query;
    search.addEventListener('input', () => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(() => filters.requestSubmit(), 500);
    });
})();
