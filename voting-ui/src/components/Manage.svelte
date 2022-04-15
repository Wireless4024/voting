<script lang="ts">
	import Button, {Label}                                 from '@smui/button';
	import DataTable, {Body, Cell, Head, Row}              from '@smui/data-table';
	import Dialog, {Actions, Content, InitialFocus, Title} from '@smui/dialog';
	import IconButton                                      from '@smui/icon-button';
	import LinearProgress                                  from '@smui/linear-progress';
	import Snackbar, {SnackbarComponentDev}                from '@smui/snackbar';
	import Textfield                                       from '@smui/textfield';
	import HelperText                                      from '@smui/textfield/helper-text';
	import format_duration                                 from 'format-duration'
	import {noop, onMount}        from "svelte/internal";
	import {getToken, loadIssues} from "../ts/common/api";
	import {TEXT_ONLY}            from "../ts/common/constants";
	import {CURRENT_VOTE_SESSION, init, LAST_SUMMARY} from "../ts/common/current_vote";
	import {DelayedExpr}                                            from "../ts/common/delayed_expr";
	import {HTTP_HOST}                                from "../ts/config";
	import {CURRENT_TIMESTAMP}                        from "../ts/interval";
	import type {Issue}                               from "../ts/types/jira";
	import type {ApiResponse}                         from "../ts/types/response";
	import type {Session}                             from "../ts/types/session";
	import type {JiraSessionSummary}                  from "../ts/types/summary";
	import Summary                                    from "./Summary.svelte";

	let loaded = true
	let dialog_open = false
	let dialog_content = ""
	let dialog_action = noop

	let selected_key = ""
	let filter = ""

	let issues: Issue[] = []

	let visible_issues = []
	const dx = new DelayedExpr(function () {
		const lower_filter = filter.toLowerCase()
		const needles = lower_filter.split(' ')
		visible_issues = issues.filter(it => it.key.toLowerCase().includes(lower_filter) || filterText(it.summary, needles))
		loaded = true
	}, 300)

	$:{
		if (issues) {
			if (!filter) {
				visible_issues = issues
			} else {
				loaded = false
				dx.dispatch(issues)
			}
		}
	}

	function filterText(heystack: string, needles: string[]) {
		for (let string of heystack.replace(TEXT_ONLY, " ").toLowerCase().split(' ')) {
			for (let needle of needles) {
				if (string.includes(needle)) return true
			}
		}
		return false
	}

	async function refreshIssue() {
		loaded = false
		const resume = Date.now() + 600
		const status: ApiResponse<boolean> = await fetch(`${HTTP_HOST}/issues/refresh?token=${getToken()}`).then(it => it.json())
		if (!status.ok || !status.body) {
			loaded = true
			return
		}
		await _loadIssues(resume)
	}

	async function _loadIssues(resume?: number) {
		if (resume == undefined) resume = Date.now() + 600
		loaded = false
		const _issues: ApiResponse<Issue[]> = (await loadIssues()) || {ok: false}
		if (!_issues.ok) return alert("Failed to load issues")
		const delay = resume - Date.now()
		await new Promise(function (ok) {
			setTimeout(ok, delay)
		})
		visible_issues = issues = _issues.body || []
		loaded = true
	}

	onMount(function () {
		init()
		_loadIssues()
	})

	function select_issue(key: string) {
		dialog_content = $CURRENT_VOTE_SESSION ? 'Do you want to end current session and start new one?' : 'Do you really want to start vote session?'
		dialog_open = true
		dialog_action = start_session
		selected_key = key
	}

	async function start_session() {
		if ($CURRENT_VOTE_SESSION) await end_session_now()
		try {
			const fd = new FormData()
			fd.append('key', selected_key)
			await fetch(`${HTTP_HOST}/create_new?token=${getToken()}`, {
				method: "POST",
				body  : fd
			})
		} catch (e) {
			console.error(e)
		}
	}

	function end_session() {
		dialog_content = 'Do you really want to end session?'
		dialog_open = true

		dialog_action = end_session_now
	}


	let closeAlert: SnackbarComponentDev;

	async function end_session_now() {
		const resp: ApiResponse<string> = await fetch(`${HTTP_HOST}/end?token=${getToken()}`).then(it => it.json())
		if (resp.ok) {
			closeAlert.open()
		}
	}

	let current: Session<number> | null

	$:current = $CURRENT_VOTE_SESSION

	let summary: JiraSessionSummary
	$:summary = $LAST_SUMMARY
</script>
<Dialog bind:open={dialog_open}
        aria-labelledby="default-focus-title"
        aria-describedby="default-focus-content">
	<Title id="default-focus-title">Action</Title>
	<Content id="default-focus-content">
		{dialog_content}
	</Content>
	<Actions>
		<Button on:click={() => {}}>
			<Label>ON</Label>
		</Button>
		<Button defaultAction
		        use={[InitialFocus]}
		        on:click={()=>dialog_action()}>
			<Label>YSE</Label>
		</Button>
	</Actions>
</Dialog>
<Snackbar bind:this={closeAlert}>
	<Label>Session has been ended.</Label>
	<Actions>
		<IconButton class="material-icons" title="Dismiss">close</IconButton>
	</Actions>
</Snackbar>
{#if summary}
	<Summary bind:summary={summary} showSave={true}/>
{/if}
{#if current}
	<div style="margin-top: 24px"></div>
	<div class="half">
		<div>
			<h1 class="center">Current session</h1>
		</div>
		<DataTable table$aria-label="User list" style="width: 100%;">
			<Head>
				<Row>
					<Cell/>
					<Cell style="width: 100%">Status</Cell>
				</Row>
			</Head>
			<Body>
			<Row>
				<Cell>Title</Cell>
				<Cell>{current.title}</Cell>
			</Row>
			<Row>
				<Cell>Participant</Cell>
				<Cell>
					{#if current.voted?.length}
						{#each current.voted as voted}
							<div class="participant"
							     title={format_duration(voted.timestamp-current.since)}>
								{voted.name}
							</div>
						{/each}
					{/if}
					({($CURRENT_VOTE_SESSION)?.voted?.length})
				</Cell>
			</Row>
			<Row>
				<Cell>Duration</Cell>
				<Cell>{format_duration($CURRENT_TIMESTAMP - ($CURRENT_VOTE_SESSION)?.since)}</Cell>
			</Row>
			<Row>
				<Cell>Action</Cell>
				<Cell>
					<Button on:click={end_session}>End session</Button>
				</Cell>
			</Row>
			</Body>
		</DataTable>
	</div>
{/if}
<div style="margin-top: 24px"></div>
<div class:half={$CURRENT_VOTE_SESSION}>
	<h1 class="center">Available issue
		<IconButton class="material-icons" on:click={refreshIssue}>refresh</IconButton>
	</h1>
	<div>
		<Textfield bind:value={filter} label="Filter" style="width: 100%">
			<HelperText slot="helper">Type keyword to filter issues</HelperText>
		</Textfield>
	</div>
	<DataTable class="inner" table$aria-label="User list" style="width: 100%;">
		<Head>
			<Row>
				<Cell>Key</Cell>
				<Cell style="width: 100%">Summary</Cell>
			</Row>
		</Head>
		<Body>
		{#each visible_issues as item (item.key)}
			<Row draggable={true} on:click={()=>select_issue(item.key)}>
				<Cell>{item.key}</Cell>
				<Cell>{item.summary}</Cell>
			</Row>
		{/each}
		</Body>

		<LinearProgress
				indeterminate
				bind:closed={loaded}
				aria-label="Data is being loaded..."
				slot="progress"
		/>
	</DataTable>
</div>
