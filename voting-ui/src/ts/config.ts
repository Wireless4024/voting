export const HOST = ''
export const HTTP_HOST = HOST?.length ? `http://${HOST}` : ''
export let UPLOAD_URL = ''

export function setUploadTarget(target: string) {
	UPLOAD_URL = target
}