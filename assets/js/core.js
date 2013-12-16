;$(function() { 
	var wrapper = $('<div class="img-wrapper"></div>');
	$('.feature-doc img').wrap(wrapper);

	$('a[href^="http"').attr('target', '_blank');
});
