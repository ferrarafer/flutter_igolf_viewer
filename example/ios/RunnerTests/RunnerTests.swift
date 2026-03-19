import XCTest
import IGolfViewer3D

@testable import flutter_igolf_viewer

final class RunnerTests: XCTestCase {

  func testSameHoleSameModeIsNoOp() {
    let plan = HoleTransitionModePlanner.makePlan(
      currentHole: 4,
      currentMode: .modeFreeCam,
      requestedHole: 4,
      requestedMode: .modeFreeCam
    )

    XCTAssertTrue(plan.isNoOp)
    XCTAssertFalse(plan.shouldUpdateHole)
    XCTAssertFalse(plan.shouldApplyModeImmediately)
    XCTAssertFalse(plan.shouldResetPostLoadModeFlag)
    XCTAssertNil(plan.pendingModeAfterHoleLoad)
  }

  func testHoleChangeToFreeCamAppliesImmediatelyAndReappliesAfterLoad() {
    let plan = HoleTransitionModePlanner.makePlan(
      currentHole: 4,
      currentMode: .modeFlyover,
      requestedHole: 5,
      requestedMode: .modeFreeCam
    )

    XCTAssertFalse(plan.isNoOp)
    XCTAssertTrue(plan.shouldUpdateHole)
    XCTAssertTrue(plan.shouldApplyModeImmediately)
    XCTAssertTrue(plan.shouldResetPostLoadModeFlag)
    XCTAssertEqual(
      plan.pendingModeAfterHoleLoad,
      PendingHoleChangeNavigationMode(hole: 5, mode: .modeFreeCam)
    )

    let resolution = HoleTransitionModePlanner.resolveModeAfterHoleLoad(
      loadedHole: 5,
      pendingMode: plan.pendingModeAfterHoleLoad,
      hasAppliedPostLoadMode: false
    )

    XCTAssertEqual(resolution, .applyPending(.modeFreeCam))
  }

  func testHoleChangeToFlyoverDefersImmediateApplyAndReappliesAfterLoad() {
    let plan = HoleTransitionModePlanner.makePlan(
      currentHole: 5,
      currentMode: .modeFreeCam,
      requestedHole: 6,
      requestedMode: .modeFlyover
    )

    XCTAssertFalse(plan.isNoOp)
    XCTAssertTrue(plan.shouldUpdateHole)
    XCTAssertFalse(plan.shouldApplyModeImmediately)
    XCTAssertTrue(plan.shouldResetPostLoadModeFlag)
    XCTAssertEqual(
      plan.pendingModeAfterHoleLoad,
      PendingHoleChangeNavigationMode(hole: 6, mode: .modeFlyover)
    )

    let resolution = HoleTransitionModePlanner.resolveModeAfterHoleLoad(
      loadedHole: 6,
      pendingMode: plan.pendingModeAfterHoleLoad,
      hasAppliedPostLoadMode: false
    )

    XCTAssertEqual(resolution, .applyPending(.modeFlyover))
  }

  func testHoleLoadWithoutPendingModeUsesBootstrapFreeCamOnce() {
    let firstResolution = HoleTransitionModePlanner.resolveModeAfterHoleLoad(
      loadedHole: 1,
      pendingMode: nil,
      hasAppliedPostLoadMode: false
    )
    XCTAssertEqual(firstResolution, .applyBootstrapFreeCam)

    let secondResolution = HoleTransitionModePlanner.resolveModeAfterHoleLoad(
      loadedHole: 1,
      pendingMode: nil,
      hasAppliedPostLoadMode: true
    )
    XCTAssertEqual(secondResolution, .keepCurrentMode)
  }
}
