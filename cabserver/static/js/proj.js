function init_calenda()
{
$(".fc-date").click(function() { 
var day=this.innerHTML;

$.ajax({
  type: "GET",
  url: "/webservice/get_drivers",
  data: { day: day}
})
  .done(function( msg ) {
//    alert( "Data Saved: " + msg );
  });
});
}

function init_drivers()
{
		
	$(".grid_4").click(function() {
		
	});
}