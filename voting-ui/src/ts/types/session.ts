export type Session<V> = {
	title: string
	voted: VoteParticipant[]
	choices: V[]
	since: number
}

export type VoteParticipant = {
	name: string
	timestamp: number
}