(() => {
    const form = document.querySelector('#donation-proposal-form');
    const confirmation = document.querySelector('#proposal-confirmation');
    const result = document.querySelector('#proposal-result');
    const registration = document.querySelector('#inventory-registration');
    const registrationForm = document.querySelector('#inventory-registration-form');

    if (!form || !confirmation || !result || !registration || !registrationForm
        || typeof confirmation.showModal !== 'function') return;

    const inventory = form.elements.namedItem('donation');
    const quantity = form.elements.namedItem('quantity');
    const submitButton = form.querySelector('button[type="submit"]');
    const useAllButton = form.querySelector('[data-use-all]');
    const selectedDetails = document.querySelector('#selected-donation');
    const emptyState = document.querySelector('#inventory-empty');
    const fulfilledState = document.querySelector('#need-fulfilled');
    let remainingQuantity = Number(form.dataset.remainingQuantity);
    const feedback = document.querySelector('#inventory-feedback');
    const quantityHint = document.querySelector('#quantity-hint');
    const registrationItem = registrationForm.elements.namedItem('item');
    const registrationQuantity = registrationForm.elements.namedItem('quantity');
    const confirmButton = confirmation.querySelector('[data-confirm-proposal]');
    let pendingProposal = null;
    let proposalSent = false;
    let registrationTrigger;
    let registeredNewDonation = false;
    let nextMockId = 1;

    const units = (count) => `${count} ${count === 1 ? 'unidade' : 'unidades'}`;
    const selectedDonation = () => inventory.selectedOptions[0];
    const isRegistered = (donation) => donation?.dataset.status === 'registered';
    const showFeedback = (message) => {
        feedback.textContent = message;
        feedback.hidden = false;
    };

    const updateAvailability = () => {
        const hasAvailableDonations = [...inventory.options].some(isRegistered);
        const fulfilled = remainingQuantity === 0;
        fulfilledState.hidden = !fulfilled;
        emptyState.hidden = fulfilled || hasAvailableDonations;
        form.hidden = fulfilled || !hasAvailableDonations;
        document.querySelectorAll('[data-open-registration]').forEach(button => { button.disabled = fulfilled; });
    };

    const updateSelection = () => {
        const donation = selectedDonation();
        const available = remainingQuantity > 0 && isRegistered(donation);
        quantity.setCustomValidity('');
        quantity.disabled = !available;
        useAllButton.disabled = !available;
        submitButton.disabled = !available;
        selectedDetails.hidden = !donation?.value;

        if (!donation?.value) {
            quantity.value = '';
            quantity.removeAttribute('max');
            quantityHint.textContent = 'Selecione uma doação para consultar a quantidade disponível.';
            return;
        }

        const stock = Number(donation.dataset.quantity);
        selectedDetails.querySelector('[data-selected-item]').textContent = donation.dataset.item;
        selectedDetails.querySelector('[data-selected-condition]').textContent = donation.dataset.condition;
        selectedDetails.querySelector('[data-selected-stock]').textContent = units(stock);
        selectedDetails.querySelector('[data-selected-description]').textContent = donation.dataset.description;
        selectedDetails.querySelector('[data-selected-status]').textContent = available ? 'Registrado' : 'Aguardando Aceite';
        selectedDetails.querySelector('[data-selected-status]').classList.toggle('proposal-inventory-status--waiting', !available);
        const maximum = Math.min(stock, remainingQuantity);
        quantity.max = String(maximum);
        quantity.value = available ? String(maximum) : '';
        useAllButton.textContent = stock <= remainingQuantity ? 'Doar todas' : 'Doar máximo';
        quantityHint.textContent = available
            ? `Você possui ${units(stock)} e a ONG ainda precisa de ${units(remainingQuantity)}. Você pode oferecer até ${units(maximum)}.`
            : 'Esta doação já tem uma proposta aguardando aceite. Selecione outra doação registrada.';
    };

    const validateQuantity = (input, maximum = Number.MAX_SAFE_INTEGER) => {
        const value = input.valueAsNumber;
        const valid = Number.isSafeInteger(value) && value >= 1 && value <= maximum;
        input.setCustomValidity(valid ? '' : `Informe uma quantidade inteira entre 1 e ${maximum}.`);
        return valid;
    };

    inventory.addEventListener('change', updateSelection);
    quantity.addEventListener('input', () => quantity.setCustomValidity(''));
    useAllButton.addEventListener('click', () => {
        if (remainingQuantity <= 0 || !isRegistered(selectedDonation())) return;
        quantity.value = String(Math.min(Number(selectedDonation().dataset.quantity), remainingQuantity));
        quantity.setCustomValidity('');
    });

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const donation = selectedDonation();
        if (remainingQuantity <= 0 || !isRegistered(donation)) return;
        const stock = Number(donation.dataset.quantity);
        if (!validateQuantity(quantity, Math.min(stock, remainingQuantity)) || !form.reportValidity()) {
            quantity.reportValidity();
            return;
        }

        const offered = quantity.valueAsNumber;
        confirmation.querySelector('[data-confirmation-item]').textContent = donation.dataset.item;
        confirmation.querySelector('[data-confirmation-quantity]').textContent = units(offered);
        confirmation.querySelector('[data-confirmation-condition]').textContent = donation.dataset.condition;
        confirmation.querySelector('[data-confirmation-remaining]').textContent = units(stock - offered);
        confirmation.querySelector('[data-confirmation-item-description]').textContent = donation.dataset.description;
        const message = form.elements.namedItem('description').value.trim();
        confirmation.querySelector('[data-confirmation-message]').textContent = message;
        confirmation.querySelector('[data-confirmation-message-row]').hidden = !message;
        pendingProposal = { donation, stock, offered };
        proposalSent = false;
        confirmButton.disabled = false;
        confirmation.showModal();
    });

    // Mock only: transition the inventory entry after the donor explicitly confirms the reviewed proposal.
    confirmButton.addEventListener('click', () => {
        if (!confirmation.open || !pendingProposal) return;
        const { donation, stock, offered } = pendingProposal;
        if (!isRegistered(donation) || Number(donation.dataset.quantity) !== stock
            || !Number.isSafeInteger(offered) || offered < 1 || offered > stock || offered > remainingQuantity) return;

        confirmButton.disabled = true;
        pendingProposal = null;
        proposalSent = true;
        donation.dataset.status = 'awaiting_acceptance';
        donation.disabled = true;
        donation.textContent = `${donation.dataset.item} • Aguardando Aceite`;
        remainingQuantity -= offered;
        form.dataset.remainingQuantity = String(remainingQuantity);
        document.querySelector('[data-need-remaining]').textContent = units(remainingQuantity);
        updateSelection();
        updateAvailability();
        showFeedback(`Proposta enviada com sucesso. ${donation.dataset.item}: ${units(offered)} na proposta. Status em Minhas Doações: Aguardando Aceite.`);
        confirmation.close();
        result.querySelector('[data-result-quantity]').textContent = units(offered);
        result.querySelector('[data-result-remaining]').textContent = units(remainingQuantity);
        result.querySelector('[data-result-message]').textContent = remainingQuantity === 0
            ? 'Sua proposta completou a quantidade necessária. Esta necessidade não aceita novas doações.'
            : `Obrigado pela sua contribuição! A ONG ainda precisa de ${units(remainingQuantity)}. Você pode continuar doando.`;
        result.querySelector('[data-close-result]').textContent = remainingQuantity === 0 ? 'Ver necessidade atendida' : 'Continuar doando';
        result.showModal();
    });

    confirmation.querySelector('[data-close-confirmation]').addEventListener('click', () => confirmation.close());
    confirmation.addEventListener('close', () => {
        pendingProposal = null;
        if (result.open) return;
        if (remainingQuantity === 0) fulfilledState.querySelector('h3').focus();
        else if (form.hidden) emptyState.querySelector('[data-open-registration]').focus();
        else if (!proposalSent) submitButton.focus();
        else inventory.focus();
    });

    result.querySelector('[data-close-result]').addEventListener('click', () => result.close());
    result.addEventListener('close', () => {
        if (remainingQuantity === 0) fulfilledState.querySelector('h3').focus();
        else if (form.hidden) emptyState.querySelector('[data-open-registration]').focus();
        else inventory.focus();
    });

    document.querySelectorAll('[data-open-registration]').forEach((button) => {
        button.disabled = false;
        button.addEventListener('click', () => {
            if (remainingQuantity <= 0) return;
            registrationTrigger = button;
            registeredNewDonation = false;
            registration.showModal();
        });
    });

    registrationItem.addEventListener('input', () => registrationItem.setCustomValidity(''));
    registrationQuantity.addEventListener('input', () => registrationQuantity.setCustomValidity(''));
    registrationForm.addEventListener('submit', (event) => {
        event.preventDefault();
        if (remainingQuantity <= 0) return;
        const item = registrationItem.value.trim();
        registrationItem.setCustomValidity(item ? '' : 'Informe o nome do item.');
        const validQuantity = validateQuantity(registrationQuantity);
        if (!validQuantity || !registrationForm.reportValidity()) {
            registrationForm.reportValidity();
            return;
        }

        const condition = registrationForm.elements.namedItem('condition');
        const stock = registrationQuantity.valueAsNumber;
        const donation = new Option(`${item} • ${units(stock)}`, `mock-donation-${nextMockId++}`, false, true);
        Object.assign(donation.dataset, {
            item,
            quantity: String(stock),
            condition: condition.selectedOptions[0].textContent,
            description: registrationForm.elements.namedItem('description').value.trim() || 'Sem descrição adicional.',
            status: 'registered'
        });
        inventory.add(donation);
        updateAvailability();
        updateSelection();
        showFeedback(`${item} cadastrado em Minhas Doações com o status Registrado. Escolha a quantidade para sua proposta.`);
        registeredNewDonation = true;
        registrationForm.reset();
        registration.close();
    });

    registration.querySelector('[data-close-registration]').addEventListener('click', () => registration.close());
    registration.addEventListener('close', () => {
        if (registeredNewDonation) inventory.focus();
        else registrationTrigger?.focus();
    });
    registrationForm.querySelector('button[type="submit"]').disabled = false;
    updateSelection();
    updateAvailability();
})();
