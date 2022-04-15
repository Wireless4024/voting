export type ApiResponse<T> = {
	ok: boolean
	error?: string
	body?: T
}

export function defaultResp<T>(): ApiResponse<T> {
	return {ok: false}
}