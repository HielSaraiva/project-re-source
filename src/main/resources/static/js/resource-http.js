(() => {
    const context = (document.querySelector('meta[name="context-path"]')?.content || '/').replace(/\/$/, '');
    const url = path => context + path;
    const json = async (path, {method = 'GET', data, signal, cache, errorMessage = 'Não foi possível concluir a operação. Atualize a página e tente novamente.'} = {}) => {
        const headers = {Accept: 'application/json'};
        if (method !== 'GET') {
            const token = document.querySelector('meta[name="_csrf"]')?.content;
            const header = document.querySelector('meta[name="_csrf_header"]')?.content;
            if (token && header) headers[header] = token;
        }
        if (data !== undefined) headers['Content-Type'] = 'application/json';
        const response = await fetch(url(path), {method, headers, credentials: 'same-origin', signal, cache,
            body: data === undefined ? undefined : JSON.stringify(data)});
        if (response.status === 204) return null;
        const body = await response.json().catch(() => null);
        if (!response.ok || response.redirected || body === null) throw new Error(body?.detail || errorMessage);
        return body;
    };
    window.ResourceHttp = Object.freeze({context, url, json});
})();
