// console.log("My Project");
// alert("tesitng");
console.log("this is script file");

const toggleSidebar = () => {
	const sidebar = $('.sidebar');
	const content = $(".content");

	if (sidebar.is(":visible")) {
		// close the sidebar
		sidebar.hide();
		content.css("margin-left", "0%");
	} else {
		// show the sidebar
		sidebar.show();
		content.css("margin-left", "20%");
	}
};

const search = () => {
	console.log("searching...");
	let query = $("#search-input").val();

	if (query == '') {
		$(".search-result").hide();
	}
	else {
		console.log(query);
		let url = `http://localhost:8080/search/${query}`;
		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			//data..  recieve the response ;
			console.log(data);
			//injecting html code in the  box  ;
		let text = `<div class='list-group'>`
		//traverse by for each ;
		data.forEach((contact) => {
			text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action text-primary' >${contact.name}</a>`
			
		});
		
        text += `</div>`

		$(".search-result").html(text);

		$(".search-result").show();


		});

		


	}
};
