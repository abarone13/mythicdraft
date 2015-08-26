var request = window.superagent;
var Router = ReactRouter;
var DefaultRoute = Router.DefaultRoute;
var Link = Router.Link;
var Route = Router.Route;
var RouteHandler = Router.RouteHandler;

var navElement = document.getElementById("navContainer");
var appElement = document.getElementById("container");

var nextDummyValue = 0; //shame

ReactModal.setAppElement(navElement);
ReactModal.injectCSS();

var UploadForm = React.createClass({
	getInitialState: function() {
		return {isSubmitDisabled: false,
			    isDraftDuplicate: false,
			    isDraftInvalid: false};
	},

	handleSubmit: function(e) {
		e.preventDefault();			
		var comp = this;		

		comp.setState({isSubmitDisabled: true});
				
		var draftFile = e.target[0].files[0];
		var deckFile = e.target[1].files[0];
			
		var req = request.post('/upload')
						 .attach('file', draftFile, draftFile.name)					
						 .field('name', this.refs.draftName.getDOMNode().value)
						 .field('wins', this.refs.draftWins.getDOMNode().value)
						 .field('losses', this.refs.draftLosses.getDOMNode().value)
			
		if(deckFile) {
			req.attach('deck', deckFile, deckFile.name);
		}
			
		req.end(function(err, resp) {
			if(!resp.body.draftInvalid && !resp.body.draftDuplicate) {
				comp.props.navbar.closeUploadModal();
			} else {
				comp.setState({isSubmitDisabled: false,
							   isDraftDuplicate: resp.body.draftDuplicate,
							   isDraftInvalid: resp.body.draftInvalid,
							   isDeckInvalid: resp.body.deckInvalid});
			}				
		});		
	},

	render: function(){
		var hasError = this.state.isDraftInvalid || this.state.isDraftDuplicate || this.state.isDeckInvalid;
		var errorText = '';
		
		if(hasError) {
			if(this.state.isDraftDuplicate) {
				errorText = 'You have already uploaded this draft.';
			} else  if(this.state.isDraftInvalid) {
				errorText = 'The file uploaded is not a valid MTGO draft log.';
			} else {
				errorText = 'The deck file uploaded contains cards not drafted, or is not a valid MTGO deck file in text form.';
			}
		}
	
		return (
			<div className="modal-content">
				<div className="modal-header">
					<button type="button" className="close" onClick={this.props.navbar.closeUploadModal}>
						<span aria-hidden="true" className="glyphicon glyphicon-remove"></span>
						<span className="sr-only margin-left">Close</span>
					</button>
					<h4 className="modal-title">Upload Draft</h4>				
				</div>
				<div className="modal-body">
					<form encType="multipart/form-data" onSubmit={this.handleSubmit} ref="uploadForm">
						<div className={hasError ? "form-group has-error" : "form-group"}>
							<label htmlFor="file">Draft to Upload</label>
							<input type="file" name="file" ref="draftFile" required></input>
							<p className="help-block">This file should be the MTGO draft log.</p>
						</div>

						<div className={hasError ? "form-group has-error" : "form-group"}>
							<label htmlFor="deck">Deck to Upload (Optional)</label>
							<input type="file" name="deck" ref="deckFile"></input>
							<p className="help-block">This file should be the deck exported in text from the MTGO freeform log.</p>
						</div>
						
						<div className="form-group">
							<label htmlFor="name">Give Your Draft a Name</label>
							<input className="form-control" type="text" name="name" ref="draftName" maxLength="20" required></input>					
						</div>

						<div className="form-group">
							<label htmlFor="wins">Rounds Won</label>
							<input className="form-control" type="number" name="wins" ref="draftWins" required></input>					
						</div>
						
						<div className="form-group">
							<label htmlFor="losses">Rounds Lost</label>
							<input className="form-control" type="number" name="losses" ref="draftLosses" required></input>					
						</div>					
											
						<button type="submit" className="btn btn-default" disabled={this.state.isSubmitDisabled}>Submit</button>					
						<span className={hasError ? 'text-danger margin-left' : ''}>{errorText}</span>
					</form>					
				</div>
			</div>
		);
	}
});

var UploadDeckForm = React.createClass({
	getInitialState: function() {
		return {isSubmitDisabled: false,
			    isDraftDuplicate: false,
			    isDraftInvalid: false};
	},

	handleSubmit: function(e) {
		e.preventDefault();			
		var comp = this;		

		comp.setState({isSubmitDisabled: true});
		
		var deckFile = e.target[0].files[0];
			
		request.post('/upload/deck')
			.attach('deck', deckFile, deckFile.name)
			.field('draftId', this.props.draftId)
			.end(function(err, resp) {
				if(!resp.body.draftInvalid && !resp.body.draftDuplicate) {
					comp.props.draftTable.closeUploadModal();
					window.location.href = "#/deck/" + resp.body.deckId;
				} else {
					comp.setState({isSubmitDisabled: false,
								   isDraftDuplicate: resp.body.draftDuplicate,
								   isDraftInvalid: resp.body.draftInvalid,
								   isDeckInvalid: resp.body.deckInvalid});
				}				
			});		
	},

	render: function(){
		var hasError = this.state.isDraftInvalid || this.state.isDraftDuplicate || this.state.isDeckInvalid;
		var draftName = this.props.draftName;
		var errorText = '';
		
		if(hasError) {
			if(this.state.isDraftDuplicate) {
				errorText = 'You have already uploaded this draft.';
			} else  if(this.state.isDraftInvalid) {
				errorText = 'The file uploaded is not a valid MTGO draft log.';
			} else {
				errorText = 'The deck file uploaded contains cards not drafted, or is not a valid MTGO deck file in text form.';
			}
		}
	
		return (
			<div className="modal-content">
				<div className="modal-header">
					<button type="button" className="close" onClick={this.props.draftTable.closeUploadModal}>
						<span aria-hidden="true" className="glyphicon glyphicon-remove"></span>
						<span className="sr-only margin-left">Close</span>
					</button>
					<h4 className="modal-title">Upload Deck for Draft {draftName}</h4>				
				</div>
				<div className="modal-body">
					<form encType="multipart/form-data" onSubmit={this.handleSubmit} ref="uploadDeckForm">

						<div className={hasError ? "form-group has-error" : "form-group"}>
							<label htmlFor="deck">Deck to Upload</label>
							<input type="file" name="deck" ref="deckFile" required></input>
							<p className="help-block">This file should be the deck exported in text from the MTGO freeform log.</p>
						</div>
											
						<button type="submit" className="btn btn-default" disabled={this.state.isSubmitDisabled}>Submit</button>					
						<span className={hasError ? 'text-danger margin-left' : ''}>{errorText}</span>
					</form>					
				</div>
			</div>
		);
	}
});

var RecentDrafts = React.createClass({
	contextTypes: {
		router: React.PropTypes.func
	},

	_updateState: function(props) {
		var comp = this;
		
		request
			.get("/draft/recent")
			.end(function(err, resp) {
				comp.setState({data: resp.body});
			});	
	},

	getInitialState: function() {
		return {data: []};
	},
	
	componentDidMount: function() {
		this._updateState(this.props);
	},
	
	componentWillReceiveProps: function(nextProps) {
		this._updateState(nextProps);
	},
	
	render: function() {
		var drafts = this.state.data;		
		
		return (
			<table className="table table-hover">
				<thead>
					<tr>
						<td>
							Draft Name
						</td>
						<td>
							Active Player
						</td>						
						<td>
							Format
						</td>
						<td>
							Wins
						</td>
						<td>
							Losses
						</td>	
						<td>
							Deck
						</td>
					</tr>				
				</thead>
				<tbody>
					{drafts.map(function(draft) {
						return <RecentDraft data={draft} />;
					})}
				</tbody>
			</table>
		);
	}
});

var RecentDraft = React.createClass({
	getInitialState: function() {
		return { uploadModalIsOpen: false };
	},

	openUploadModal: function() {
		this.setState({ uploadModalIsOpen: true });
	},

	closeUploadModal: function() {
		this.setState({ uploadModalIsOpen: false });
		
	},

	render: function() {
		var packsString = "";
		var packsJson = JSON.stringify(this.props.data.packs);
		
		this.props.data.packs.map(function(aPack) {
			packsString += aPack.setCode + " ";
		});
	
		var uploadCell;
		
		if(this.props.data.deckId) {
			uploadCell = <Link className="btn btn-xs btn-primary" to={"/deck/" + this.props.data.deckId}><span className="glyphicon glyphicon-eye-open"></span><span className="margin-left">View Deck</span></Link>;
		} else {
			uploadCell = <UploadDeckButton data={this.props.data} draftTable={this} />;
		}
	
		return (
			<tr>
				<td>
					<Link to={"/draft/" + this.props.data.id + "/pack/" + this.props.data.packs[0].id + "/pick/0"}>
						{this.props.data.name}
					</Link>					
				</td>
				<td>
					<Link to={"/draft/player/" + this.props.data.activePlayer.id}>
						{this.props.data.activePlayer.name}
					</Link>					
				</td>				
				<td>
					<span>
						{packsString}
					</span>
					<a href={'#/draft/' + this.props.data.eventId}>
					</a>
				</td>
				<td>
					{this.props.data.wins}
				</td>
				<td>
					{this.props.data.losses}
				</td>				
				<td>
					{uploadCell}
				</td>
			</tr>
		);
	}
});

var UploadDeckButton = React.createClass({

	render: function() {
		return (
			<span>
				<a className="btn btn-xs btn-warning" href="#" role="button" onClick={this.props.draftTable.openUploadModal} >
					<span className="glyphicon glyphicon-upload"></span><span className="margin-left">Upload Deck</span>
				</a>
				<ReactModal className="Modal__Bootstrap modal-dialog" 
							isOpen={this.props.draftTable.state.uploadModalIsOpen}
							onRequestClose={this.props.draftTable.closeUploadModal}>
					<UploadDeckForm draftTable={this.props.draftTable} draftId={this.props.data.id} draftName={this.props.data.name}/>
				</ReactModal>		
			</span>
		);
	}

})

var Deck = React.createClass({
	contextTypes: {
		router: React.PropTypes.func
	},

	_updateState: function(props) {
		var comp = this;
		
		request
			.get("/draft/" + props.params.deckId + "/deck")
			.end(function(err, resp) {
				comp.setState({data: resp.body});
			});	
	},

	getInitialState: function() {
		return { data: {
				mainDeckCards: [],
				sideBoardCards: [],
				draftId: 0
			}
		};
	},
	
	componentDidMount: function() {
		this._updateState(this.props);
	},
	
	componentWillReceiveProps: function(nextProps) {
		this._updateState(nextProps);
	},

	render: function() {
		return (
			<div className="row">
			
			</div>
		);
	}

});

var PlayerDrafts = React.createClass({
	contextTypes: {
		router: React.PropTypes.func
	},

	_updateState: function(props) {
		var comp = this;
		
		request
			.get("/draft/player/" + props.params.playerId)
			.end(function(err, resp) {
				comp.setState({data: resp.body});
			});	
	},

	getInitialState: function() {
		return {data: {
			drafts: [],
			player: {},
			winPercentage: ""
		}};
	},
	
	componentDidMount: function() {
		this._updateState(this.props);
	},
	
	componentWillReceiveProps: function(nextProps) {
		this._updateState(nextProps);
	},
	
	render: function() {
		var drafts = this.state.data.drafts;		
		var player = this.state.data.player;		
		var winPercentage = this.state.data.winPercentage;
		
		return (
			<div className="container-fluid">
				<div className="row">
					<div className="col-xs-12 col-md-3">
						<h2 className="text-info text-center">
							{player.name}
						</h2>
						<div className="row well">
							<div className="col-xs-8">
								Total Drafts Uploaded
							</div>
							<div className="col-xs-4">
								{drafts.length}
							</div>
						</div>
						<div className="row well">
							<div className="col-xs-8">
								Win Percentage
							</div>
							<div className="col-xs-4">
								{winPercentage}
							</div>						
						</div>						
					</div>
					<div className="col-xs-12 col-md-9">
						<table className="table table-hover">
							<thead>
								<tr>
									<td>
										Draft Name
									</td>
									<td>
										Format
									</td>
									<td>
										Wins
									</td>
									<td>
										Losses
									</td>						
								</tr>				
							</thead>
							<tbody>
								{drafts.map(function(draft) {
									return <PlayerDraft data={draft} />;
								})}
							</tbody>
						</table>							
					</div>
				</div>
			</div>
		);
	}
});

var PlayerDraft = React.createClass({
	render: function() {
		var packsString = "";
		var packsJson = JSON.stringify(this.props.data.packs);
		
		this.props.data.packs.map(function(aPack) {
			packsString += aPack.setCode + " ";
		});
	
		return (
			<tr>
				<td>
					<Link to={"/draft/" + this.props.data.id + "/pack/" + this.props.data.packs[0].id + "/pick/0"}>
						{this.props.data.name}
					</Link>					
				</td>
				<td>
					<span>
						{packsString}
					</span>
				</td>
				<td>
					{this.props.data.wins}
				</td>
				<td>
					{this.props.data.losses}
				</td>				
			</tr>
		);
	}
});

var PlayerSearch = React.createClass({
	getInitialState: function() {
		return { players: [], searchDisabled: false };
	},
	
	handleChange: function(event) {
		var searchString = event.target.value;
	
		//If search is disabled it's because we're waiting on an ajax response
		if(this.state.searchDisabled) {
			return;
		}else if(!searchString || searchString.length < 3) {
			this.setState({ players: [] });
			return;
		} else {
			this.setState({ searchDisabled: true });
		}
	
		var comp = this;
	
		request.get('/player/search')
			.query({ name: searchString })
			.end(function(err, resp){
				comp.setState({ players: resp.body, searchDisabled: false });
			});
	},
	
	handleKeyPress: function(event) {				
		if(event.keyCode == 13) {
			var playerId = 0;
			var selectedPlayerName = React.findDOMNode(this.refs.playerInput).value;
			var playersList = React.findDOMNode(this.refs.playersList).children;
			
			for(var i = 0; i < playersList.length; i++){
				var playerNode = playersList[i];
				
				if(playerNode.value == selectedPlayerName){
					playerId = playerNode.dataset.playerId;
					break;
				}
			}
			
			window.location.hash = "#/draft/player/" + playerId;
			event.preventDefault();			
		}		
	},
	
	render: function() {
		var players = this.state.players;
	
		return (
			<form className="navbar-form navbar-left" role="search">
				<div className="form-group">
					<input type="text" 
						   className="form-control" 
						   placeholder="Player Search" 
						   onInput={this.handleChange} 
						   onKeyDown={this.handleKeyPress}
						   ref="playerInput"
						   list="players" />
					<datalist onChange={this.handlePlayerSelect} id="players" ref="playersList">
						{players.map(function(aPlayer, idx) {
							return <option key={aPlayer.id} 
										   data-player-id={aPlayer.id}
										   value={aPlayer.name}>
								   </option>;
						}, this)}
					</datalist>
				</div>
			</form>
		);
	}
});

var NavBar = React.createClass({
	getInitialState: function() {
		return { uploadModalIsOpen: false };
	},

	openUploadModal: function() {
		this.setState({ uploadModalIsOpen: true });
	},

	closeUploadModal: function() {
		this.setState({ uploadModalIsOpen: false });
		this.props.app.forceUpdate(nextDummyValue++);
	},
	
	componentDidMount: function() {
		bindNavbarToggle();
	},
	
	render: function() {
		return (
			<nav className="navbar navbar-default">
				<div className="container-fluid">
					<div className="navbar-header">
						<a className="navbar-brand" href="#">Mythic Draft</a>
						<button type="button" 
								className="navbar-toggle collapsed" 
								data-toggle="collapse" 
								data-target="#collapsable-navbar" 
								aria-expanded="false">
							<span className="sr-only">Toggle navigation</span>
							<span className="icon-bar"></span>
							<span className="icon-bar"></span>
							<span className="icon-bar"></span>								
						</button>
					</div>
					<div className="collapse navbar-collapse" id="collapsable-navbar">
						<ul className="nav navbar-nav">
							<li onClick={this.openUploadModal} className="visible-md-block visible-lg-block">
								<a href="#">
									<span className="glyphicon glyphicon-upload"></span><span className="margin-left">Upload</span>
								</a>
							</li>
							<ReactModal className="Modal__Bootstrap modal-dialog" 
										isOpen={this.state.uploadModalIsOpen}
										onRequestClose={this.closeUploadModal}>
								<UploadForm navbar={this} />
						    </ReactModal>
						</ul>
						
						<PlayerSearch />
						
						<ul className="nav navbar-nav navbar-right visible-md-block visible-lg-block">
							<img className="navbar-ad" src="http://placehold.it/728x90" alt="Leaderboard Ad" />											
						</ul>
					</div>									
				</div>								
			</nav>
		);
	}
});

var Draft = React.createClass({
	_updateState: function(props) {
		var comp = this;
		var draftId = props.params.draftId;
		var packId = props.params.packId;
		var pickNumber = props.params.pickId;
		
		request.get('/draft/' + draftId + '/pack/' + packId + '/pick/' + pickNumber)
			.end(function(err, resp) {		
				var currentPack;
				
				for(var i = 0; i < resp.body.draftMetaData.packs.length; i++){
					if(packId == resp.body.draftMetaData.packs[i].id){
						currentPack = i;
						break;
					}
				}
			
				comp.setState({data: resp.body, 
							   packs: resp.body.draftMetaData.packs, 
							   currentPack: currentPack, 
							   pickNumber: pickNumber, 
							   isPickShown: props.isPickShown});
			});	
	},

	componentWillReceiveProps: function(nextProps) {
		nextProps.isPickShown = false;
		this._updateState(nextProps);
	},
	
	contextTypes: {
		router: React.PropTypes.func
	},
	
	getInitialState: function() {
		return {data: []};
	},
	
	componentDidMount: function() {
		this._updateState(this.props);
	},
	
	render: function() {
		if(!this.state.data.pick) {
			return <div></div>;
		} 
	
		var pick = this.state.data.pick;	
		var isPickShown = this.state.isPickShown;
		var available = this.state.data.available;
		var pickNumber = this.state.pickNumber;
		var packNumber = this.state.currentPack;
		var packs = this.state.packs;
		
		return (
			<div className="container-fluid">
				<DraftControls draft={this} />
				<h1>Pack {Number(packNumber) + 1} Pick {Number(pickNumber) + 1}</h1>
				<CardRow ref="cardRow" cards={available} pick={pick} isPickShown={isPickShown} />
				<h1>All Player Picks</h1>
				<AllPlayerPicks pickNumber={pickNumber} packNumber={packNumber} draftId={this.props.params.draftId} packs={packs} />
			</div>
		);
	}
});	

var DraftControls = React.createClass({
	showPick: function() {
		this.props.draft.setState({isPickShown: true});
	},
	
	linkHandler: function(event) {
		var link = React.findDOMNode(this.refs.shareLink);
		link.focus();
		link.select();
	},
	
	render: function() {
		var currentPackSize = Number(this.props.draft.state.packs[this.props.draft.state.currentPack].packSize);
		var previousDisabled = Number(this.props.draft.state.pickNumber) == 0 && Number(this.props.draft.state.currentPack) == 0 ? 'disabled' : '';
		var nextDisabled = Number(this.props.draft.state.pickNumber) == currentPackSize - 1 && Number(this.props.draft.state.currentPack) == 2 ? 'disabled' : '';
	
		var draftId = this.props.draft.state.data.draftMetaData.id;
		var nextPickNumber = Number(this.props.draft.state.pickNumber) + 1;
		var previousPickNumber = Number(this.props.draft.state.pickNumber) - 1;
		var packs = this.props.draft.state.packs;
		var currentPack = this.props.draft.state.currentPack;
		var nextPackId = packs[currentPack].id;
		var previousPackId = packs[currentPack].id;
			
		var linkToThis = "http://" + window.location.host + "/#/draft/" + draftId + "/pack/" + nextPackId + "/pick/" + this.props.draft.state.pickNumber;
			
		if(!nextDisabled && nextPickNumber == packs[currentPack].packSize) {
			nextPackId = packs[currentPack + 1].id;
			nextPickNumber = 0;
		} 

		if(previousPickNumber == -1 && currentPack != 0) {
			previousPackId = packs[currentPack - 1].id;
			previousPickNumber = packs[currentPack - 1].packSize - 1;
		} 	
	
		return (
			<div className="row">
				<div className="col-md-1 col-md-offset-3 col-xs-4">
					<Link className={previousDisabled ? "visibility-hidden btn btn-sm btn-warning" : "btn btn-sm btn-warning"}
						  to={"/draft/" + draftId + "/pack/" + previousPackId + "/pick/" + previousPickNumber} >
						Previous
					</Link>
				</div>
				<div className="col-md-1 col-xs-4">					
					<button type="button" 
							onClick={this.showPick}
							className="btn btn-sm btn-info">
						Show Pick
					</button>			
				</div>
				<div className="col-md-1 col-xs-4">										
					<Link className={nextDisabled ? "visibility-hidden btn btn-sm btn-success" : "btn btn-sm btn-success"}
						  to={"/draft/" + draftId + "/pack/" + nextPackId + "/pick/" + nextPickNumber} >
						Next
					</Link>										
				</div>
				<div className="col-md-3 col-md-offset-0 col-xs-10 col-xs-offset-1">
					<div onClick={this.linkHandler} className="input-group input-group-sm">
						<span className="input-group-addon glyphicon glyphicon-share"></span>
						<input ref="shareLink" type="text" className="form-control" aria-describedby="basic-addon1" value={linkToThis}/>						
					</div>											
				</div>																	
			</div>
		);
	}
});

var CardRow = React.createClass({
	render: function() {
		var cards = this.props.cards;		
		var pick = this.props.pick;
		var isPickShown = this.props.isPickShown;
		var counter = 0;
		
		return (
			<div className="row top-buffer no-pad">				
				{cards.map(function(aCard) {
					
					return <div className={counter++ % 5 == 0 ? "col-md-offset-1 col-md-2 col-sm-3 col-xs-4" : "col-md-2 col-sm-3 col-xs-4"}>
						       <Card multiverseId={aCard.multiverseId} 
									 key={aCard.id} 
									 isPick={aCard.id == pick ? true : false} 
									 isPickShown={isPickShown} />
							</div>;
				})}				
			</div>			
		);
	}
});

var Card = React.createClass({
	render: function() {
		var classString;
	
		if(this.props.isPickShown) {
			if(this.props.isPick) {
				classString = 'img-responsive';
			} else {
				classString = 'img-responsive card-not-picked-animation';
			}
		} else {
			classString = 'img-responsive';
		}
	
		return (
			<img className={classString}
				 src={"http://gatherer.wizards.com/Handlers/Image.ashx?type=card&multiverseid=" + this.props.multiverseId} />
		);
	}
});

var AllPlayerPicks = React.createClass({
	getInitialState: function() {
		return { allCards: [] , picksInOrder: []};
	},

	componentDidMount: function() {
		var comp = this;
		var draftId = this.props.draftId;
		
		request.get('/draft/' + draftId + '/all')
			.end(function(err, resp) {							
				comp.setState({allCards: resp.body.allCards, picksInOrder: resp.body.picksInOrder});
			});		
	},

	render: function() {
		var pickIdsInOrder = this.state.picksInOrder;
		var cardIds = this.state.allCards;
		var packNumber = this.props.packNumber;
		var pickNumber = this.props.pickNumber;
		
		return (
			<div className="row">
				{pickIdsInOrder.map(function(aCardId, index) {
					return  <div className="col-md-1 col-xs-3">
								<Card multiverseId={aCardId} 
									  key={aCardId + index} />
							</div>;
				})}
				
				<div className="hidden">		
					{cardIds.map(function(aCardId) {
						return <img src={"http://gatherer.wizards.com/Handlers/Image.ashx?type=card&multiverseid=" + aCardId} />;
					})}
				</div>				
			</div>
		);
	}
});

var App = React.createClass({
	forceUpdate: function(dummyValue) {
		this.setState({update: dummyValue});
	},
	
	render: function() {
		return (
			<div>
				<NavBar app={this}/>
				<RouteHandler />
			</div>
		);
	}
});

var routes = (
  <Route name="app" path="/" handler={App}>
    <Route name="draft" path="draft/:draftId/" handler={Draft} >
		<Route name="pack" path="pack/:packId/" handler={Draft} >
			<Route name="pick" path="pick/:pickId" handler={Draft} >
			</Route>
		</Route>
	</Route>
	
	<Route name="player" path="draft/player/:playerId" handler={PlayerDrafts} />

	<Route name="deck" path="deck/:deckId" handler={Deck} />
	
    <DefaultRoute handler={RecentDrafts}/>
  </Route>
);

Router.run(routes, function (Handler) {
  React.render(<Handler/>, appElement);
});