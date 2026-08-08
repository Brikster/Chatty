'use strict';

// In-game chat test for scripts/smoke-test.sh.
// Connects two bots to a running server, makes one of them send a few chat
// messages (plain text, a mention, a link, a private message and a reply) and
// verifies the other bot — and the sender itself — receive them processed by
// Chatty.

const mineflayer = require('mineflayer');

const HOST = process.env.BOT_HOST || '127.0.0.1';
const PORT = parseInt(process.env.BOT_PORT || '25565', 10);

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function connect(username) {
    return new Promise((resolve, reject) => {
        const bot = mineflayer.createBot({
            host: HOST, port: PORT, username, auth: 'offline', version: false,
        });
        const timer = setTimeout(() => reject(new Error(username + ' did not spawn within 60s')), 60000);
        const finish = (err) => { clearTimeout(timer); err ? reject(err) : resolve(bot); };
        bot.once('spawn', () => finish());
        bot.once('error', (e) => finish(new Error(username + ' connection error: ' + e.message)));
        bot.once('kicked', (reason) => finish(new Error(username + ' was kicked: ' + JSON.stringify(reason))));
    });
}

function assert(condition, message) {
    if (!condition) {
        throw new Error(message);
    }
}

// Recursively walks a parsed chat-component tree (text, extra[], with[], …)
// looking for a clickEvent with the given action. Chatty turns a mention into
// a <click:suggest_command:...> component, so the action lands on a nested
// child of the message rather than the root.
function hasClickAction(node, action) {
    if (!node || typeof node !== 'object') {
        return false;
    }
    const click = node.clickEvent || node.click_event;
    if (click && (click.action === action)) {
        return true;
    }
    for (const child of [].concat(node.extra || [], node.with || [])) {
        if (hasClickAction(child, action)) {
            return true;
        }
    }
    return false;
}

async function main() {
    const sender = await connect('SmokeSender');
    const target = await connect('SmokeTarget');

    const lines = [];
    const messages = [];
    for (const bot of [sender, target]) {
        bot.on('messagestr', (str) => lines.push(str));
        bot.on('message', (msg) => {
            // msg.json is the raw component; keep it for structural assertions.
            if (msg && msg.json) {
                messages.push(msg.json);
            }
        });
    }

    await sleep(1500); // let both bots settle in the world

    sender.chat('hello from the smoke test');
    await sleep(900);
    sender.chat('@SmokeTarget you have been mentioned');
    await sleep(900);
    sender.chat('check out https://example.com today');
    await sleep(1200);
    sender.chat('/msg SmokeTarget a private word');
    await sleep(1200);
    target.chat('/r and a private answer');
    await sleep(2500);

    const text = lines.join('\n');

    sender.quit();
    target.quit();

    const mentionInteractive = messages.some((m) => hasClickAction(m, 'suggest_command'));

    if (!text.includes('hello from the smoke test')
            || !text.includes('you have been mentioned')
            || !text.includes('check out https://example.com today')
            || !text.includes('a private word')
            || !text.includes('and a private answer')
            || !mentionInteractive) {
        // Dump everything observed so a failure is diagnosable from CI logs.
        console.error('--- observed chat lines (' + lines.length + ') ---');
        lines.forEach((l) => console.error('  ' + JSON.stringify(l)));
        console.error('--- observed components (' + messages.length + ') ---');
        messages.forEach((m) => console.error('  ' + JSON.stringify(m)));
        console.error('------------------------------------------');
    }

    assert(text.includes('hello from the smoke test'), 'plain chat message was not delivered');
    assert(text.includes('you have been mentioned'), 'mention message was not delivered');
    assert(text.includes('check out https://example.com today'), 'link message was not delivered');
    assert(mentionInteractive, 'mention was not turned into an interactive component');
    assert(text.includes('a private word'), 'private message from /msg was not delivered');
    assert(text.includes('and a private answer'), 'reply from /r was not delivered');

    console.log('CHAT TEST OK (' + lines.length + ' chat lines observed)');
    process.exit(0);
}

main().catch((e) => {
    console.error('CHAT TEST FAILED: ' + (e && e.message ? e.message : e));
    process.exit(1);
});
