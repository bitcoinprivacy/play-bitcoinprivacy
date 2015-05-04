$(function() {
  initAllValues()
  formatUnitSwitchButtons()
  $(".show_btc").click()
  setAllLinks();

  // show to Top button for large scroll sites 
  if ( ($(window).height() + 100) < $(document).height() ) {
    $('#top-link-block').removeClass('hidden').affix({
        // how far to scroll down before link "slides" into view
        offset: {top:100}
    });
}
});

function formatUnitSwitchButtons()
{
    formatButton("show_btc", "BTC", 8)
    formatButton("show_sat", "sat", 0)
    formatButton("show_bit", "bit", 2)
    formatButton("show_mil", "mBTC", 5)
}



function setAllLinks()
{
    setAddressInWalletLinks();
    setBlockExplorerLinks();
    setTransactionLinks();
    setWalletLinks();
    setPaidInTxLinks();
    setSpentInTxLinks();
}

function setPaidInTxLinks()
{
    $(".bge_paid_in").each(function(index, element){setPaidInTxLink(index, element)});
}

function setSpentInTxLinks()
{
    $(".bge_spent_in").each(function(index, element){setSpentInTxLink(index, element)});
}


function setPaidInTxLink(index, element)
{
    //alert("x"+element.getAttribute("value")+"x")
    var a = document.createElement("a");
    a.href = "/transaction/"+element.getAttribute("value");
    a.innerHTML = element.innerHTML
    element.innerHTML = ""
    a.style.color="green";
    a.style.textDecoration="underline";
    element.appendChild(a);
}

function setSpentInTxLink(index, element)
{
    //alert("x"+element.getAttribute("value")+"x")
    var a = document.createElement("a");
    a.href = "/transaction/"+element.getAttribute("value");
    a.innerHTML = element.innerHTML
    element.innerHTML = ""
    a.style.color="red";
    a.style.textDecoration="underline";
    element.appendChild(a);
}


function setAddressInWalletLinks()
{
    $(".bge_address_wallet").each(function(index, element){setAddressInWalletLink(index, element)});
}


function setWalletLinks()
{
    $(".bge_wallet").each(function(index, element){setAddressLink(index, element)});
}
        
function setWalletLink(index, element)
{
    var a = document.createElement("a");
    a.href = "/wallet/"+element.innerHTML;
    a.innerHTML = element.innerHTML;
    element.innerHTML = "";
    element.appendChild(a);
}

function setAddressLink(index, element)
{
    var a = document.createElement("a");
    a.href = "/wallet/"+element.innerHTML;
    a.innerHTML = element.innerHTML;
    element.innerHTML = "";
    element.appendChild(a);
}
        
function setTransactionLinks()
{
    $(".bge_tx_block").each(function(index, element){setTransactionLink(index, element)});
}

function setTransactionLink(index, element)
{
    var a = document.createElement("a");
    a.href = "/transaction/"+element.innerHTML
    a.innerHTML = element.innerHTML;
    element.innerHTML = ""
    element.appendChild(a);
}

function setBlockExplorerLinks()
{
    $(".bge_block").each(function(index, element){setBlockLink(index, element)});
}

function setBlockLink(index, element)
{
    var a = document.createElement("a");
    a.href = "/block/"+element.innerHTML;
    a.innerHTML = element.innerHTML;
    element.innerHTML = ""
    element.appendChild(a);
}

function setAddressInWalletLink(index, element)
{
    var a = document.createElement("a");
    a.href = "/address/"+element.innerHTML;
    a.innerHTML = element.innerHTML;
    element.innerHTML = "";
    element.appendChild(a);
}

function setSelected(className)
{
    $(".show_unit").removeClass("active")
    $("."+className).addClass("active")
}

function formatButton(className, label, positions)
{
   $("."+className).html(label).click(function(){formatAllValues(positions); setSelected(className); return false;})
}

function formatAllValues(d)
{
    $(".bge_value").each(function(i, v){formatBitcoinValues(i,v,d)})
}

function initAllValues()
{
    $(".bge_value").each(function(i,e){e.setAttribute("satoshis", e.innerHTML)})
}

function formatBitcoinValues(index, element, divisor)
{
    element.innerHTML = parseFloat(element.getAttribute("satoshis")/Math.pow(10, divisor)).toFixed(divisor)
}

