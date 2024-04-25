(clear-all)

(setf *allow-external-connections* t)

; Run X seconds
;(run 5.0 t)

(define-model pepper-mood

;(sgp :trace-filter production-firing-only :v "tracer.txt")
(sgp :trace-filter nil :v "tracer.txt")

(chunk-type goal next_a pepper_out state end mood)

(add-dm
	(waiting-for-pepper)
	(waiting) (welcoming)
	(goal isa goal state waiting) (photos-true) (photos-false) (pepper-done) (model-done)
	(clearing-pepper-out) (checking-intention) (retrieving-requirements) (checking-requirements) (checking-requirements-c) (checking-requirements-zwei) (checking-requirements-zwei-p)
	(pepper-checks-intention) (pepper-retrieves-requirements) (pepper-finds-complication) (pepper-finishes-checking-requirements)
	(content) (joyful) (sad) (angry) (pepper-content) (pepper-joyful) (pepper-sad) (pepper-angry) (pepper-changes-mood) (pepper-changed-mood) (pepper-start-mood)
	
	(checking-intention-chunk isa goal next_a checking-intention)
	(retrieving-requirements-chunk isa goal next_a retrieving-requirements)
	(person-has-photos isa goal state photos-true)
	(person-has-no-photos isa goal next_a checking-requirements-c state photos-false)
	(checking-requirements-zwei-chunk isa goal next_a checking-requirements-zwei state pepper-done)
	(checking-requirements-zwei-p-chunk isa goal next_a checking-requirements-zwei-p state pepper-done)
	(mood-content-chunk isa goal mood content state pepper-changes-mood)
	(mood-welcoming-content-chunk isa goal mood content state pepper-start-mood)
	(mood-joyful-chunk isa goal mood joyful state pepper-changes-mood)
	(mood-welcoming-joyful-chunk isa goal mood joyful state pepper-start-mood)
	(mood-sad-chunk isa goal mood sad state pepper-changes-mood)
	(mood-welcoming-sad-chunk isa goal mood sad state pepper-start-mood)
	(mood-angry-chunk isa goal mood angry state pepper-changes-mood)
	(mood-welcoming-angry-chunk isa goal mood angry state pepper-start-mood)
)

(goal-focus goal)

(p starting-interaction
	=goal>
		isa			goal
		state		waiting
==>
	=goal>
		next_a		waiting-for-pepper
	!output!	"Starting interaction"
)

(p waiting-for-pepper
	=goal>
		isa 		goal
		next_a		waiting-for-pepper
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	waiting-for-pepper
	!output!	"Waiting for Pepper"
)

(p checking-intention
	=goal>
		isa			goal
		next_a		checking-intention
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	pepper-checks-intention
	!output!	"Checking intention"
)

(p clearing-pepper-out
	=goal>
		isa 		goal
		next_a		clearing-pepper-out
==>
	=goal>
		pepper_out	nil
;	!output!	"Pepper out nil"
)

(p retrieving-requirements
	=goal>
		isa			goal
		next_a		retrieving-requirements
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	pepper-retrieves-requirements
	!output!	"Retrieving requirements"
)

(p requirements-check-c
	=goal>
		isa			goal
		next_a		checking-requirements-c
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	pepper-finds-complication
	!output!	"Requirements check c"
)

(p checking-requirements-zwei
	=goal>
		isa			goal
		next_a		checking-requirements-zwei
		state		pepper-done
==>
	=goal>
		pepper_out	pepper-finishes-checking-requirements
		state		model-done
	!output!	"Model done"
)

(p checking-requirements-zwei-p
	=goal>
		isa			goal
		next_a		checking-requirements-zwei-p
		state		pepper-done
==>
	=goal>
		pepper_out	pepper-finishes-checking-requirements
		state		model-done
	!output!	"Model done"
)

(p pepper-content
	=goal>
		isa			goal
		mood		content
		state		pepper-changes-mood
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	pepper-content
		state		pepper-changed-mood
	!output!	"Model content"
)

(p pepper-welcoming-content
	=goal>
		isa			goal
		mood		content
		state		pepper-start-mood
==>
	=goal>
		next_a		waiting-for-pepper
		pepper_out	pepper-content
		state		pepper-changed-mood
	!output!	"Model welcoming content"
)

(p pepper-joyful
	=goal>
		isa			goal
		mood		joyful
		state		pepper-changes-mood
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	pepper-joyful
		state		pepper-changed-mood
	!output!	"Model joyful"
)

(p pepper-welcoming-joyful
	=goal>
		isa			goal
		mood		joyful
		state		pepper-start-mood
==>
	=goal>
		next_a		waiting-for-pepper
		pepper_out	pepper-joyful
		state		pepper-changed-mood
	!output!	"Model welcoming joyful"
)

(p pepper-sad
	=goal>
		isa			goal
		mood		sad
		state		pepper-changes-mood
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	pepper-sad
		state		pepper-changed-mood
	!output!	"Model sad"
)

(p pepper-welcoming-sad
	=goal>
		isa			goal
		mood		sad
		state		pepper-start-mood
==>
	=goal>
		next_a		waiting-for-pepper
		pepper_out	pepper-sad
		state		pepper-changed-mood
	!output!	"Model welcoming sad"
)

(p pepper-angry
	=goal>
		isa			goal
		mood		angry
		state		pepper-changes-mood
==>
	=goal>
		next_a		clearing-pepper-out
		pepper_out	pepper-angry
		state		pepper-changed-mood
	!output!	"Model angry"
)

(p pepper-welcoming-angry
	=goal>
		isa			goal
		mood		joyful
		state		pepper-start-mood
==>
	=goal>
		next_a		waiting-for-pepper
		pepper_out	pepper-angry
		state		pepper-changed-mood
	!output!	"Model welcoming angry"
)

)