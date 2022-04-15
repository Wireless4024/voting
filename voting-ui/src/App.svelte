<script lang="ts">
	import Router, {querystring, replace} from 'svelte-spa-router'
	import {onMount}                      from "svelte/internal";
	import {checkToken, TOKEN}            from "./ts/common/api";
	import routes                         from "./ts/routes";

	let token
	$:token = $TOKEN

	onMount(async function () {
		const rawToken = new URLSearchParams($querystring || window.location.search).get("token")
		
		if (!token) {
			if (await checkToken(rawToken)) {
				console.info("valid token!")
				token = $TOKEN = rawToken
			}
		}
		if (rawToken){
			location.replace('/')
		}
	})
</script>
<!-- nav goes here -->
{#if token}
	<main class="container">
		<Router {routes}/>
	</main>
{:else}
	1 + 1 = 3
{/if}
