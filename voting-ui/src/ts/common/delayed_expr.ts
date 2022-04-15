export class DelayedExpr {
	private lastPerform = 0

	constructor(private action: () => any, private delay: number) {}

	dispatch(..._) {
		const current = this.lastPerform = Date.now()
		this.schedule(current)
	}

	private schedule(lock) {
		const self = this
		setTimeout(function () {
			if (self.lastPerform == lock) self.action()
		}, this.delay)
	}
}