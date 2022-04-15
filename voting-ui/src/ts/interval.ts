import {writable} from "svelte/store";

export const CURRENT_TIMESTAMP = writable(Date.now())
setInterval(function () {CURRENT_TIMESTAMP.set(Date.now())}, 1)