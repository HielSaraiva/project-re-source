(() => {
    window.resourceMockStorage = {
        create(key, validate) {
            let memory = null;
            let useMemory = false;
            return {
                read() {
                    if (useMemory) return memory;
                    try { return validate(JSON.parse(sessionStorage.getItem(key))); }
                    catch { return memory; }
                },
                write(state) {
                    memory = validate(state);
                    try { sessionStorage.setItem(key, JSON.stringify(memory)); }
                    catch { useMemory = true; }
                }
            };
        }
    };
})();
