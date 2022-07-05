import {writable}                from "svelte/store";
import {HOST, HTTP_HOST}         from "../config";
import type {ApiResponse}        from "../types/response";
import type {Session}            from "../types/session";
import type {JiraSessionSummary} from "../types/summary";
import {getToken, loadSummary}   from "./api";

export const CURRENT_VOTE_SESSION = writable<Session<number> | null>()
export const LAST_SUMMARY_KEY = writable<string | null>()
export const LAST_SUMMARY = writable<JiraSessionSummary | null>()
LAST_SUMMARY_KEY.subscribe(k => k && loadSummary(k).then(it => LAST_SUMMARY.set(it)))

async function fetchCurrentSession() {
	const resp: ApiResponse<Session<number>> = await fetch(`${HTTP_HOST}/current_session?token=${getToken()}`).then(it => it.json())
	CURRENT_VOTE_SESSION.set(resp.body)
	const url = new URL('/ws/current_session', HTTP_HOST || window.location.href);
	url.protocol = url.protocol.replace('http', 'ws');

	const socket = new WebSocket(url.href)
	await new Promise(function (ok) {
		socket.addEventListener('open', ok)
	})
	console.info("connected")
	socket.addEventListener('message', function (event) {
		const {data} = event
		if (data?.length === 0) return
		if (!data)
			CURRENT_VOTE_SESSION.set(null)
		else {
			const object = JSON.parse(data)
			if (typeof object === 'string')
				LAST_SUMMARY_KEY.set(object)
			else
				CURRENT_VOTE_SESSION.set(object)
		}
	});

	socket.addEventListener('close', () => tryRecon(false))
	socket.addEventListener('error', () => tryRecon(false))
}

async function tryRecon(first = true) {
	while (true)
		try {
			await recon(first)
			break
		} catch (e) {
			console.error(e)
			first = false
		}
}

function recon(first?: boolean): Promise<void> {
	CURRENT_VOTE_SESSION.set(null)
	if (!first) {
		console.error("retry to connect in 3 sec")

		return new Promise(function (ok, err) {
			setTimeout(function () {
				fetchCurrentSession().then(ok).catch(err)
			}, 3000)
		})
	} else {
		return fetchCurrentSession()
	}
}

export function init() {
	return tryRecon()
}