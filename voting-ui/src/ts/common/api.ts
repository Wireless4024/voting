import {writable}                               from "svelte/store";
import {HTTP_HOST, setUploadTarget, UPLOAD_URL} from "../config";
import type {Issue}                             from "../types/jira";
import type {ApiResponse}                       from "../types/response";
import type {ServerConfig}                      from "../types/server_config";
import type {JiraSessionSummary}                from "../types/summary";

export const TOKEN = writable<string>(localStorage.getItem("voting-token"))
TOKEN.subscribe(it => it && localStorage.setItem("voting-token", it))

export function getToken() {
	return localStorage.getItem("voting-token")
}

export async function checkToken(token: string): Promise<boolean> {
	return fetch(`${HTTP_HOST}/check-token?token=${token}`).then<ApiResponse<boolean>>(it => it.json()).then(it => it.body)
}

export async function loadIssues(): Promise<ApiResponse<Issue[]>> {
	return fetch(`${HTTP_HOST}/issues?token=${getToken()}`).then(it => it.json())
}


export async function loadSummary(key: string): Promise<JiraSessionSummary> {
	return fetch(`${HTTP_HOST}/summary/${key}?token=${getToken()}`).then(it => it.json())
}

export async function loadConfig() {
	const config: ApiResponse<ServerConfig> = await fetch(`${HTTP_HOST}/config.json?token=${getToken()}`).then(it => it.json())
	setUploadTarget(config?.body?.upload_url)
}

export async function fileUpload(file: File, fieldName: string = "file") {
	if (!UPLOAD_URL || !file) return Promise.reject("Missing upload url")
	const fd = new FormData()
	fd.append(fieldName, file)
	return fetch(`${UPLOAD_URL}`, {
		method: 'POST',
		body  : fd
	})
}