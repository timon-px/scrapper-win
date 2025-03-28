const styles = `
    #${arguments[0]} {
        position: fixed;
        z-index: 9999;
        top: 16px;
        right: 16px;
        padding: 12px;
        color: #ffffff;
        border: 1px solid #ff2400;
        border-radius: 4px;
        background-color: #ff2400;
        font-family: Arial;
        transition: all .15s;
    }
        
    #${arguments[0]}:hover {
        cursor: pointer;
        background-color: #c41e3a;
        box-shadow: rgba(100, 100, 111, 0.2) 0px 7px 29px 0px;
    }
    #${arguments[0]}:active {
        box-shadow: none;
    }
    #${arguments[0]}.saved {
        border-color: #6082b6;
        background-color: #6082b6;
    }
    #${arguments[0]}.saved:hover {
        background-color: #7393b3;
    }
    #${arguments[0]}.ready {
        border-color: #50c878;
        background-color: #50c878;
    }
    #${arguments[0]}.ready:hover {
        background-color: #2aaa8a;
    }
`;

// Append styles for button
let styleBlock = document.createElement("style");
styleBlock.setAttribute(`data-${arguments[0]}`, "")
styleBlock.insertAdjacentHTML("afterbegin", styles)
document.head.appendChild(styleBlock);

// Append button
const captureButton = document.createElement('button');
captureButton.id = arguments[0];
captureButton.setAttribute(`data-${arguments[0]}`, "")
captureButton.textContent = 'Capture';
document.body.appendChild(captureButton);

// Track HTML captures
window.isReadyToSave = false;
window.isAlreadySaved = false;
window.uniqueCaptureButtonId = arguments[0];

function setReadyStyle() {
    if (captureButton.classList.contains("saved"))
        captureButton.classList.remove("saved");

    if (!captureButton.classList.contains("ready"))
        captureButton.classList.add("ready");
}

function setSavedStyle() {
    if (captureButton.classList.contains("ready"))
        captureButton.classList.remove("ready");

    if (!captureButton.classList.contains("saved"))
        captureButton.classList.add("saved");
}

function setUniqueStyle() {
    if (captureButton.classList.contains("ready"))
        captureButton.classList.remove("ready");
    if (captureButton.classList.contains("saved"))
        captureButton.classList.remove("saved");
}

window.setSavedHtml = () => {
    window.isReadyToSave = false;
    window.isAlreadySaved = true;
    setSavedStyle();
}

// MutationObserver to detect DOM changes
const observer = new MutationObserver((mutations) => {
    const isButtonChange = mutations.every(mutation =>
        mutation.target === captureButton ||
        mutation.target.parentNode === captureButton
    );
    if (!isButtonChange) {
        window.isReadyToSave = false;
        window.isAlreadySaved = false;
        setUniqueStyle();
    }
});
observer.observe(document.body, {childList: true, subtree: true, attributes: true});

// Button click handler
captureButton.addEventListener('click', () => {
    setSavedStyle();

    // Update button color
    if (!window.isReadyToSave && !window.isAlreadySaved) {
        window.isReadyToSave = true;
        setReadyStyle();
    }
});

window.getAllStyles = () => {
    let styles = '';

    // Get styles from all stylesheets
    for (let i = 0; i < document.styleSheets.length; i++) {
        let sheet = document.styleSheets[i];

        try {
            let rules = sheet.cssRules || sheet.rules;
            for (let j = 0; j < rules.length; j++) {
                styles += rules[j].cssText + '\n';
            }
        } catch (e) {
            console.warn('Could not access stylesheet: ', sheet.href);
        }
    }

    return styles;
}