export type SessionSummary<T, V> = {
	title: T,
	choices: V[]
	votes: {
		name: string
		choice: V
		timestamp: number
	}[]
	since: number
}

export type JiraTitle = {
	key: string
	summary: string
}

export type JiraSessionSummary = SessionSummary<JiraTitle, number>