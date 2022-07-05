<script lang="ts">
	import Button                                     from "@smui/button";
	import LayoutGrid, {Cell}                         from '@smui/layout-grid';
	import Textfield                                  from "@smui/textfield";
	import format_duration                            from "format-duration";
	import {onMount}                                  from "svelte/internal";
	import {getToken}                                 from "../ts/common/api";
	import {CURRENT_VOTE_SESSION, init, LAST_SUMMARY} from "../ts/common/current_vote";
	import {HTTP_HOST}                                from "../ts/config";
	import {CURRENT_TIMESTAMP}                        from "../ts/interval";
	import {Session}                                  from "../ts/types/session";
	import type {JiraSessionSummary}                  from "../ts/types/summary";
	import Summary                                    from "./Summary.svelte";

	onMount(function () {
		init()
	})

	let name: string = localStorage.getItem("name") || ""

	$:{
		localStorage.setItem("name", name)
	}

	async function vote(choice) {
		if (!name) return alert("name is empty!")
		console.log("voted", choice)
		const fd = new FormData()
		fd.append('name', name)
		fd.append('choice', choice)
		await fetch(`${HTTP_HOST}/vote?token=${getToken()}`, {
			method: "POST",
			body  : fd
		})
	}

	let current: Session<number>
	$:current = $CURRENT_VOTE_SESSION

	let summary: JiraSessionSummary
	$:summary = $LAST_SUMMARY
</script>
{#if summary}
	<Summary {summary}/>
{/if}
<div class="center">
	{#if current}
		<h1>{current.title}</h1>
		<hr>
		<h2>Voted by {current.voted?.length}
			({format_duration($CURRENT_TIMESTAMP - ($CURRENT_VOTE_SESSION)?.since)})</h2>
		<hr>
		{#if !current.voted?.filter(it => it.name === name)?.length}
			<div>
				<Textfield bind:value={name} label="name" style="width: 80%"/>
			</div>
			<div>
				<LayoutGrid>
					{#each current.choices as choice}
						<Cell spanDevices={{ desktop: 3, tablet: 4, phone: 4 }}>
							<div class="demo-cell" style="height: 80px;" on:click|self|trusted={()=>vote(choice)}>
								<Button variant="raised" style="padding: 16pt 24pt">{choice}</Button>
							</div>
						</Cell>
					{/each}
				</LayoutGrid>
			</div>
		{:else}
			{current.voted.map(it => it.name)}
		{/if}
	{:else}
		<h1>waiting someone to start vote session..</h1>
		<img src="https://media0.giphy.com/media/u5eXlkXWkrITm/200.gif" alt="waiting.gif">
	{/if}
</div>