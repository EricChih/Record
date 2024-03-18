let timeoutWarningTimer;
let sessionTimeoutTimer;
let warningDialog;
const SESSION_EXPIRE_TIME = (30 * 60 * 1000) - 10000; // 30 分鐘 -10秒(留一個緩衝時間)
const WARNING_TIME = 20 * 60 * 1000; // 20分鐘跳警告

function startTimeoutWarningTimer() {
    timeoutWarningTimer = setTimeout(() => {
        showConfirmationDialog(`10分鐘後將逾期登入，是否要延長登入時間`, function (result) {
            if(result){
                refreshSession();
            }
        })
    }, WARNING_TIME);
}

function startSessionTimeoutTimer(){
    sessionTimeoutTimer = setTimeout(() => {
        warningDialog.modal('hide');
        window.location.href = '/perform_logout'
    }, SESSION_EXPIRE_TIME)

}

function showConfirmationDialog(msg, callback) {
    warningDialog = bootbox.confirm({
        title: '訊息',
        size: 'lg',
        message: msg,
        callback: function (result) {
            if (typeof callback === 'function') {
                callback(result);
            }
        }
    });
    warningDialog.on('shown.bs.modal', function () {
        const modalBackdrop = $('.modal-backdrop');
        const currentOpacity = modalBackdrop.css('opacity');
        const newOpacity = parseFloat(currentOpacity) + 1;
        modalBackdrop.css('opacity', newOpacity);
    });
}

function refreshSession() {
    fetch(`/user/extendSession`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.text()
        })
        .then(data => {
            if(data === 'success'){
                console.log('Refresh Session');
                restartTimer();
            }
        })
        .catch(error => {
            console.error('Fetch error:', error);
            throw error;
        });
}

function restartTimer(){
    clearTimeout(sessionTimeoutTimer);
    clearTimeout(timeoutWarningTimer);
    startTimeoutWarningTimer();
    startSessionTimeoutTimer();
}

function handleGlobalAjaxError(xhr){
    if (xhr.getResponseHeader('Content-Type').indexOf('text/html') !== -1 ||　xhr.status === 401) {
        window.location.href = 'login?msg=session_invalid';
    }
}

function setupAjaxHandler(){
    $(document).ajaxComplete(function(event, jqXHR, ajaxOptions) {
        restartTimer();
    });

    $(document).ajaxError(function(event, jqXHR, ajaxOptions, data) {
        console.log('ajax error', jqXHR);
        // 全域處理 ajax error
        handleGlobalAjaxError(jqXHR);
    })

}

$(document).ready(function (){
    startSessionTimeoutTimer();
    startTimeoutWarningTimer();
    setupAjaxHandler();
})