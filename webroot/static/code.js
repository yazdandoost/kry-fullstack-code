const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service/' + getCurrentUser());
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(service.name + ' : ' + service.status + ' '));
    var button = document.createElement("button");
    button.innerHTML = "delete";
    button.addEventListener("click", function() {
      fetch('/service/' + service.id, {
        method: 'delete',
        headers: {
          'Accept': 'application/json, text/plain, */*',
          'Content-Type': 'application/json'
        }
      }).then(res => location.reload())
    });
    li.appendChild(button);
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let serviceName = document.querySelector('#name-name').value;
    let urlName = document.querySelector('#url-name').value;
    let currentUser = getCurrentUser();
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({user: currentUser, name:serviceName, url:urlName})
}).then(res => {
      if(res.ok){
        location.reload();
      } else {
        let messageError = document.querySelector('#message');
        res.json().then(body => messageError.innerText = body.error);
      }
    });
}


function getCurrentUser() {
  let user = getCookie('kry-user')
  if (user == null) {
    user = '' + Math.floor(Math.random() * 100000)
    setCookie('kry-user', user, 1)
  }
  return user;
}

function setCookie(name,value,days) {
  var expires = "";
  if (days) {
    var date = new Date();
    // date.setTime(date.getTime() + (days*24*60*60*1000));
    date.setTime(date.getTime() + (days*24*60*60*1000));
    expires = "; expires=" + date.toUTCString();
  }
  document.cookie = name + "=" + (value || "")  + expires + "; path=/";
}
function getCookie(name) {
  var nameEQ = name + "=";
  var ca = document.cookie.split(';');
  for(var i=0;i < ca.length;i++) {
    var c = ca[i];
    while (c.charAt(0)===' ') c = c.substring(1,c.length);
    if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length,c.length);
  }
  return null;
}
