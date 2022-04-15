<script lang="ts">
	import Button, {Label}                    from "@smui/button";
	import Card, {Actions, Content}           from '@smui/card';
	import DataTable, {Body, Cell, Head, Row} from "@smui/data-table";
	import Dialog, {InitialFocus, Title}      from "@smui/dialog";
	import LayoutGrid, {Cell as LCell}        from '@smui/layout-grid';
	import formatDuration                     from "format-duration";
	import {getToken}                         from "../ts/common/api";
	import {LAST_SUMMARY, LAST_SUMMARY_KEY}   from "../ts/common/current_vote";
	import {HTTP_HOST}                        from "../ts/config";
	import type {JiraSessionSummary}          from "../ts/types/summary";

	export let summary: JiraSessionSummary
	export let showSave: boolean = false

	type SessionVoteSummary = {
		choice: string,
		count: number
	}

	let voteSummary: SessionVoteSummary[] = []

	function buildSummary(summary: JiraSessionSummary): SessionVoteSummary[] {
		const table: Record<string, number> = {}
		for (let vote of summary.votes) {
			if (table[vote.choice]) {
				table[vote.choice] += 1
			} else {
				table[vote.choice] = 1
			}
		}
		const res = Object.entries(table).map(it => ({choice: it[0], count: it[1]}))
		res.sort((a, b) => b.count - a.count)
		return res
	}

	function requestToSave() {
		dialog_open = true
	}

	function saveToJira() {
		if (summary.title.key) {
			const fd = new FormData()
			fd.append("point", voteSummary[0].choice)
			fetch(`${HTTP_HOST}/issue/${summary.title.key}/update?token=${getToken()}`, {
				method: "POST",
				body  : fd
			}).then(() => ($LAST_SUMMARY = null))
		}
	}

	$:if (summary) voteSummary = buildSummary(summary)
	let dialog_open: boolean
</script>
<Dialog bind:open={dialog_open}
        aria-labelledby="default-focus-title"
        aria-describedby="default-focus-content">
	<Title id="default-focus-title">Confirm</Title>
	<Content id="default-focus-content">
		Do you want to set point to {voteSummary?.[0]?.choice}?
	</Content>
	<Actions style="text-align: right;margin-left: auto">
		<Button on:click={() =>(dialog_open=false)}>
			<Label>ON</Label>
		</Button>
		<Button defaultAction
		        use={[InitialFocus]}
		        on:click={()=>saveToJira()}>
			<Label>YSE</Label>
		</Button>
	</Actions>
</Dialog>
<div class="card-container">
	<Card>
		<div style="text-align: center">
			<h1>Summary</h1>
			<h2 class="mdc-typography--headline6" style="margin: 0;">
				{summary.title.key} {summary.title.summary}
			</h2>
		</div>
		<Content style="padding-top: 0">
			<div class="card-media-16x9">
				<LayoutGrid>
					<LCell spanDevices={{ desktop: 8, tablet: 8, phone: 4 }}>
						<div>
							<h4 style="text-align: center">Participants</h4>
							<DataTable table$aria-label="User list" style="width: 100%;">
								<Head>
									<Row>
										<Cell style="width: 100%">Participant</Cell>
										<Cell>Time</Cell>
										<Cell>Choice</Cell>
									</Row>
								</Head>
								<Body>
								{#each summary.votes as vote}
									<Row>
										<Cell>{vote.name}</Cell>
										<Cell title={new Date(vote.timestamp).toLocaleTimeString('en-GB')}>{formatDuration(vote.timestamp - summary.since)}</Cell>
										<Cell>{vote.choice}</Cell>
									</Row>
								{/each}
								</Body>
							</DataTable>
						</div>
					</LCell>
					<LCell spanDevices={{ desktop: 4, tablet: 8, phone: 4 }}>
						<div>
							<h4 style="text-align: center">Ranking</h4>
							<DataTable table$aria-label="User list" style="width: 100%;">
								<Head>
									<Row>
										<Cell style="width: 100%">Choice</Cell>
										<Cell>Count</Cell>
									</Row>
								</Head>
								<Body>
								{#each voteSummary as v}
									<Row>
										<Cell>{v.choice}</Cell>
										<Cell>{v.count}</Cell>
									</Row>
								{/each}
								</Body>
							</DataTable>
						</div>
					</LCell>
				</LayoutGrid>
			</div>
		</Content>
		<Actions style="text-align:right;margin: auto">
			{#if showSave}
				<Button href="{HTTP_HOST}/summary/{$LAST_SUMMARY_KEY}" target="_blank">View raw json</Button>
				<Button on:click={requestToSave}>Save result to jira</Button>
			{/if}
			<Button on:click={()=>($LAST_SUMMARY=null)}>Close</Button>
		</Actions>
	</Card>
</div>