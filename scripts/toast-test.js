'use strict';

// Toast (advancement pop-up) test for scripts/smoke-test.sh.
// Connects one bot to a running server whose notifications.yml has the
// "advancements" section enabled, and waits for the server to award one of
// Chatty's toast advancements. That award is what makes the client show the
// pop-up, so observing it end-to-end is the only way to tell the feature
// works without a rendering client.
//
// It also asserts the shape Chatty relies on: the toast is a child of a
// display-less root (so no tab appears in the advancement screen) and its
// display asks for a toast without announcing to chat.

const mineflayer = require('mineflayer');

const HOST = process.env.BOT_HOST || '127.0.0.1';
const PORT = parseInt(process.env.BOT_PORT || '25565', 10);
const WAIT_SECONDS = parseInt(process.env.TOAST_WAIT || '40', 10);

function assert(condition, message) {
    if (!condition) {
        throw new Error(message);
    }
}

function main() {
    return new Promise((resolve, reject) => {
        const bot = mineflayer.createBot({
            host: HOST, port: PORT, username: 'SmokeToast', auth: 'offline', version: false,
        });

        const declared = new Map();
        const awarded = new Set();

        bot._client.on('advancements', (packet) => {
            for (const entry of packet.advancementMapping || []) {
                const key = String(entry.key);
                if (key.startsWith('chatty:')) {
                    declared.set(key, entry.value);
                }
            }
            for (const entry of packet.progressMapping || []) {
                const key = String(entry.key);
                if (key.startsWith('chatty:') && !key.endsWith('/root')) {
                    awarded.add(key);
                }
            }
        });

        const timer = setTimeout(() => {
            try {
                assert(awarded.size > 0,
                    'no toast was awarded, so no pop-up would appear: either the notification '
                    + 'never registered, or awarding it failed — on Folia that means the '
                    + 'regionised scheduler was not used');

                const roots = [...declared].filter(([, value]) => !value.parentId);
                assert(roots.length === 1,
                    'expected exactly one Chatty root advancement, found ' + roots.length);
                assert(!roots[0][1].displayData,
                    'the root advancement has a display, which gives it a tab in the advancement screen');

                for (const [key, value] of declared) {
                    if (key.endsWith('/root')) {
                        continue;
                    }
                    assert(value.parentId, key + ' is a root, so it would add its own tab');
                    assert(value.displayData, key + ' has no display, so it cannot show a toast');
                    assert(value.displayData.flags.show_toast, key + ' does not ask for a toast');
                }

                console.log('TOAST TEST OK (' + awarded.size + ' toast(s) awarded, '
                    + declared.size + ' advancement(s) declared)');
                bot.quit();
                resolve();
            } catch (e) {
                console.error('--- declared advancements ---');
                for (const [key, value] of declared) {
                    console.error('  ' + key + ' parent=' + (value.parentId || 'none')
                        + ' display=' + !!value.displayData);
                }
                console.error('--- awarded: ' + [...awarded].join(', '));
                bot.quit();
                reject(e);
            }
        }, WAIT_SECONDS * 1000);

        const bail = (err) => { clearTimeout(timer); reject(err); };
        bot.once('error', (e) => bail(new Error('connection error: ' + e.message)));
        bot.once('kicked', (reason) => bail(new Error('kicked: ' + JSON.stringify(reason))));
    });
}

main().then(() => process.exit(0)).catch((e) => {
    console.error('TOAST TEST FAILED: ' + (e && e.message ? e.message : e));
    process.exit(1);
});
