(() => {
    const {url} = window.ResourceHttp;
    const page = document.querySelector('[data-match-protocol]');
    const protocol = page?.dataset.matchProtocol;
    const showError = (error, container = document.querySelector('main')) => {
        let feedback = container.querySelector('[data-backend-error]');
        if (!feedback) {
            feedback = document.createElement('p'); feedback.dataset.backendError = '';
            feedback.className = 'backend-error'; feedback.setAttribute('role', 'alert');
            feedback.tabIndex = -1; container.prepend(feedback);
        }
        feedback.textContent = error.message; feedback.focus();
    };
    const post = async (path, data, button, container) => {
        if (button?.disabled) return null;
        const finishLoading = window.ResourceLoading.request(container, button);
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 20000);
        try {
            return await window.ResourceHttp.json(path, {method: 'POST', data, signal: controller.signal});
        } catch (error) {
            if (error.name === 'AbortError' || error instanceof TypeError) error = new Error('Não foi possível confirmar o resultado a tempo. Atualize a página e consulte o histórico antes de enviar novamente.');
            showError(error, container); return null;
        } finally { clearTimeout(timeout); finishLoading(); }
    };
    const close = (dialog, selector) => dialog?.querySelector(selector)?.addEventListener('click', () => dialog.close());
    const text = (container, selector, value) => { const el = container?.querySelector(selector); if (el) el.textContent = value ?? ''; };
    const result = (dialog, title, description, saved) => {
        if (!dialog) { window.ResourceLoading.reload(); return; }
        const statuses = {
            awaiting_acceptance: ['Aguardando aceite', 'waiting'],
            awaiting_shipment: ['Aguardando envio', 'waiting'],
            awaiting_delivery: ['Aguardando entrega', 'waiting'],
            awaiting_ngo_confirmation: ['Aguardando confirmação da ONG', 'waiting'],
            in_transit: ['Enviado', 'shipped'], completed: ['Concluído', 'completed'],
            cancelled: ['Doação cancelada', 'cancelled'], rejected: ['Recusado', 'rejected']
        };
        const badge = dialog.querySelector('[data-dialog-status]');
        if (badge && statuses[saved?.status]) {
            const [label, style] = statuses[saved.status];
            badge.textContent = label; badge.className = `donation-status donation-status--${style}`;
        }
        text(dialog, 'h2', title); text(dialog, '.ui-dialog-description, #intention-result-description', description);
        dialog.addEventListener('close', () => window.ResourceLoading.reload(), { once: true });
        dialog.showModal();
    };
    const date = value => value?.split('-').reverse().join('/') || 'Não informada';
    const units = n => `${n} ${n === 1 ? 'unidade' : 'unidades'}`;
    const data = form => Object.fromEntries(new FormData(form));

    // Review is local; quantities, identifiers and transitions are always validated by the server.
    const proposal = document.querySelector('#donation-proposal-form');
    if (proposal) {
        const review = document.querySelector('#proposal-confirmation');
        const success = document.querySelector('#proposal-result');
        const registration = document.querySelector('#inventory-registration');
        const registerForm = document.querySelector('#inventory-registration-form');
        const inventory = proposal.elements.donation;
        const quantity = proposal.elements.quantity;
        const remaining = Number(proposal.dataset.remainingQuantity);
        const update = () => {
            const selected = inventory.selectedOptions[0]; const stock = Number(selected?.dataset.quantity || 0);
            quantity.disabled = stock <= 0 || remaining <= 0;
            quantity.max = String(Math.min(stock, remaining));
            quantity.value = stock > 0 ? quantity.max : '';
            proposal.querySelector('[type="submit"]').disabled = stock <= 0 || remaining <= 0;
            proposal.querySelector('[data-use-all]').disabled = stock <= 0 || remaining <= 0;
            const details = document.querySelector('#selected-donation'); details.hidden = !selected?.value;
            for (const [key, value] of Object.entries({ item: selected?.dataset.item, condition: selected?.dataset.condition, stock: units(stock), description: selected?.dataset.description, status: 'Registrado' })) text(details, `[data-selected-${key}]`, value);
            text(document, '#quantity-hint', `Você pode oferecer até ${units(Math.min(stock, remaining))}.`);
            document.querySelector('#need-fulfilled').hidden = remaining > 0;
            document.querySelectorAll('[data-open-registration]').forEach(b => b.disabled = remaining <= 0);
        };
        inventory.addEventListener('change', update); update();
        proposal.querySelector('[data-use-all]').addEventListener('click', () => quantity.value = quantity.max);
        proposal.addEventListener('submit', event => {
            event.preventDefault(); if (!proposal.reportValidity()) return;
            const selected = inventory.selectedOptions[0];
            for (const [key, value] of Object.entries({ item: selected.dataset.item, quantity: units(Number(quantity.value)), condition: selected.dataset.condition, remaining: units(Number(selected.dataset.quantity) - Number(quantity.value)), 'item-description': selected.dataset.description, message: proposal.elements.description.value })) text(review, `[data-confirmation-${key}]`, value);
            review.querySelector('[data-confirmation-message-row]').hidden = !proposal.elements.description.value.trim();
            review.showModal();
        });
        review.querySelector('[data-confirm-proposal]').addEventListener('click', async event => {
            if (!proposal.reportValidity()) return;
            const body = data(proposal); body.need = Number(body.need); body.donation = Number(body.donation); body.quantity = Number(body.quantity);
            const saved = await post('/donor/api/proposals', body, event.currentTarget, review);
            if (!saved) return;
            review.close();
            text(success, '[data-result-quantity]', units(saved.allocatedQuantity));
            text(success, '[data-result-remaining]', units(saved.need.remainingQuantity));
            text(success, '[data-result-message]', 'A proposta foi registrada. A ONG tem 7 dias para aceitar.');
            result(success, 'Proposta enviada', 'Sua proposta foi registrada. Acompanhe o aceite da ONG pelo painel.', saved);
        });
        close(review, '[data-close-confirmation]'); close(success, '[data-close-result]');
        document.querySelectorAll('[data-open-registration]').forEach(b => b.addEventListener('click', () => registration.showModal()));
        close(registration, '[data-close-registration]');
        registerForm.querySelector('[type="submit"]').disabled = false;
        registerForm.addEventListener('submit', async event => {
            event.preventDefault(); if (!registerForm.reportValidity()) return;
            const body = data(registerForm); body.need = Number(body.need); body.quantity = Number(body.quantity);
            const saved = await post('/donor/api/donations', body, registerForm.querySelector('[type="submit"]'), registration);
            if (!saved) return;
            const option = new Option(`${saved.item} • ${units(saved.availableQuantity)}`, saved.id, false, true);
            Object.assign(option.dataset, { item: saved.item, quantity: String(saved.availableQuantity), condition: registerForm.elements.condition.selectedOptions[0].textContent, description: saved.description || 'Sem descrição adicional.', status: 'registered' });
            inventory.add(option); proposal.hidden = false; document.querySelector('#inventory-empty').hidden = true;
            registerForm.reset(); registration.close(); update();
        });
    }

    const cancelButton = document.querySelector('[data-open-cancellation]');
    const cancellation = document.querySelector('#donation-cancellation, #shipping-cancellation');
    if (cancelButton && cancellation) {
        cancelButton.hidden = page.dataset.canCancel !== 'true';
        cancelButton.addEventListener('click', () => cancellation.showModal());
        close(cancellation, '[data-close-cancellation]');
        cancellation.querySelector('[data-confirm-cancellation]').addEventListener('click', async event => {
            const saved = await post(`/donor/api/matches/${protocol}/cancel`, { confirmed: true }, event.currentTarget, cancellation);
            if (!saved) return;
            cancellation.close(); const success = document.querySelector('#cancellation-result');
            if (success) {
                const cancelledAt = new Date(saved.cancelledAt).toLocaleString('pt-BR', { timeZone: 'America/Fortaleza', dateStyle: 'short', timeStyle: 'short' }).replace(', ', ' às ');
                success.querySelectorAll('[data-cancellation-date]').forEach(el => { el.textContent = cancelledAt; el.dateTime = saved.cancelledAt; });
                result(success, 'Doação cancelada', 'O cancelamento foi registrado e a reserva foi liberada.', saved); close(success, '[data-close-result]');
            }
            else window.ResourceLoading.navigate(url(`/donor/donation/status?protocol=${protocol}`));
        });
    }
    const methodReview = document.querySelector('#delivery-method-confirmation');
    if (methodReview) {
        let method; let target;
        const current = page.dataset.matchMethod;
        document.querySelectorAll('[data-delivery-method]').forEach(card => {
            if (current) card.hidden = card.dataset.deliveryMethod !== current;
            card.querySelector('.action-button').addEventListener('click', event => {
                if (current) return;
                event.preventDefault(); method = card.dataset.deliveryMethod; target = event.currentTarget.href;
                text(methodReview, '#method-title', method === 'carrier' ? 'Confirmar envio pelos Correios' : 'Confirmar entrega presencial');
                text(methodReview, '[data-method-status]', method === 'carrier' ? 'Aguardando envio' : 'Aguardando entrega');
                methodReview.showModal();
            });
        });
        close(methodReview, '[data-cancel-method]');
        methodReview.querySelector('[data-confirm-method]').addEventListener('click', async event => {
            if (await post(`/donor/api/matches/${protocol}/method`, { method, confirmed: true }, event.currentTarget, methodReview)) window.ResourceLoading.navigate(target);
        });
    }
    const personal = document.querySelector('#in-person-delivery-form');
    const carrier = document.querySelector('#carrier-delivery-form');
    const deliveryForm = personal || carrier;
    if (deliveryForm) {
        const review = document.querySelector(personal ? '#delivery-review' : '#carrier-review');
        const success = document.querySelector(personal ? '#delivery-result' : '#carrier-result');
        const reported = ['awaiting_ngo_confirmation', 'in_transit', 'completed'].includes(page.dataset.matchStatus);
        const report = document.querySelector(personal ? '#delivery-report' : '#carrier-report');
        const sent = document.querySelector(personal ? '#delivery-sent' : '#carrier-sent');
        report.hidden = reported; sent.hidden = !reported;
        const submit = document.querySelector(`[type="submit"][form="${deliveryForm.id}"]`);
        submit.hidden = reported; submit.disabled = page.dataset[personal ? 'canConfirmPersonal' : 'canConfirmShipment'] !== 'true';
        if (reported) {
            text(sent, '[data-sent-date]', date(personal ? page.dataset.deliveredOn : page.dataset.postedOn));
            text(sent, '[data-sent-recipient]', page.dataset.receivedBy); text(sent, '[data-sent-tracking]', page.dataset.trackingCode);
            text(sent, '[data-sent-notes]', page.dataset.deliveryNotes);
            const notes = sent.querySelector('[data-sent-notes-row]'); if (notes) notes.hidden = !page.dataset.deliveryNotes;
            document.querySelector('[data-active-deadline]').hidden = true;
            text(page, personal ? '[data-delivery-status]' : '[data-carrier-status]', personal ? 'Aguardando confirmação da ONG' : 'Enviado');
            if (carrier) page.querySelector('[data-carrier-status]').classList.add('delivery-status--shipped');
            document.querySelector('[data-sent-panel-link]').hidden = false;
        }
        deliveryForm.addEventListener('submit', event => {
            event.preventDefault(); if (!deliveryForm.reportValidity()) return;
            const body = data(deliveryForm);
            text(review, '[data-review-date]', date(body.date)); text(review, '[data-review-recipient]', body.recipient);
            text(review, '[data-review-tracking]', body.trackingCode?.toUpperCase()); text(review, '[data-review-notes]', body.notes);
            review.querySelector('[data-review-notes-row]').hidden = !body.notes?.trim();
            review.showModal();
        });
        close(review, '[data-close-review]'); close(success, '[data-close-result]');
        review.querySelector(personal ? '[data-confirm-delivery]' : '[data-confirm-posting]').addEventListener('click', async event => {
            const body = data(deliveryForm); body[personal ? 'delivered' : 'posted'] = true;
            const saved = await post(`/donor/api/matches/${protocol}/${personal ? 'in-person' : 'shipment'}`, body, event.currentTarget, review);
            if (!saved) return;
            review.close(); result(success, personal ? 'Entrega informada com sucesso' : 'Postagem informada com sucesso', 'O registro foi salvo. Aguarde a confirmação de recebimento pela ONG.', saved);
        });
    }

    const decision = document.querySelector('#intention-decision');
    if (decision) {
        const form = decision.querySelector('form'); let choice;
        document.querySelectorAll('[data-open-decision]').forEach(button => button.addEventListener('click', () => {
            choice = button.dataset.openDecision; form.reset();
            const rejection = choice === 'reject'; const receiving = choice === 'receive';
            form.elements.reason.required = rejection; form.elements.receiptConfirmed.required = receiving;
            form.querySelector('[data-reason-field]').hidden = !rejection;
            form.querySelector('[data-receipt-attestation]').hidden = !receiving;
            form.querySelector('[data-postal-review]').hidden = !receiving || document.querySelector('[data-intention-detail]').dataset.deliveryMethod !== 'carrier';
            const decisionIcon = decision.querySelector('[data-dialog-icon]');
            if (decisionIcon) decisionIcon.src = url(receiving ? '/images/donor/donation-status/gift.svg' : rejection ? '/images/shared/icons/info.svg' : '/images/donor/donation-status/check-circle.svg');
            text(form, '[data-confirm-decision]', receiving ? 'Confirmar recebimento' : rejection ? 'Recusar proposta' : 'Aceitar proposta');
            text(decision, '#decision-title', receiving ? 'Confirmar recebimento?' : rejection ? 'Recusar proposta?' : 'Aceitar proposta?');
            text(decision, '#decision-description', receiving ? 'Confirme após conferir e receber todos os itens. A doação será concluída.' : rejection ? 'Informe o motivo da recusa. Esta ação não pode ser desfeita.' : 'Ao aceitar, o doador terá 7 dias para escolher a modalidade de entrega.');
            const confirm = form.querySelector('[data-confirm-decision]'); confirm.classList.toggle('ui-button--danger', rejection); confirm.classList.toggle('ui-button--primary', !rejection);
            decision.showModal();
        }));
        close(decision, '[data-close-decision]');
        form.addEventListener('submit', async event => {
            event.preventDefault(); if (!form.reportValidity()) return;
            const body = choice === 'reject' ? { reason: form.elements.reason.value } : choice === 'receive' ? { receiptConfirmed: form.elements.receiptConfirmed.checked } : { confirmed: true };
            const saved = await post(`/ong/api/matches/${protocol}/${choice === 'receive' ? 'receipt' : choice}`, body, form.querySelector('[data-confirm-decision]'), decision);
            if (!saved) return;
            decision.close(); const success = document.querySelector('#intention-result');
            result(success, choice === 'receive' ? 'Recebimento confirmado' : choice === 'reject' ? 'Proposta recusada' : 'Proposta aceita', 'A operação foi registrada com data e responsável no histórico.', saved);
            close(success, '[data-close-result]');
        });
    }
    const filters = document.querySelector('#intention-filters');
    if (filters) {
        filters.querySelectorAll('select').forEach(select => select.addEventListener('change', () => filters.requestSubmit()));
        filters.querySelector('[data-clear-filters]').addEventListener('click', event => { event.preventDefault(); window.ResourceLoading.navigate(url('/ong/donations')); });
    }
})();
