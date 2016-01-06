package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import static com.dg.ssrl.Components.*;

class PlayerBrain implements Brain {

	private static final String tag = "PlayerBrain";

    private final PlayerInputAdapter playerInputAdapter;
    private final Scheduler scheduler;
    private final Assets.Sounds sounds;

	public PlayerBrain(PlayerInputAdapter playerInputAdapter, Scheduler scheduler, Assets.Sounds sounds) {
        this.playerInputAdapter = playerInputAdapter;
        this.scheduler = scheduler;
        this.sounds = sounds;
    }

    @Override
    public boolean act(final World world) {
        boolean acted = false;

        final Entity player = world.getEntity(world.playerEntityId);
        final MoveAnimation moveAnimation = player.getComponent(MoveAnimation.class);

        if (!moveAnimation.isBusy()) {
            Direction moveDirection = playerInputAdapter.getMovementDirection();

            if (moveDirection != Direction.NONE) {
                //Gdx.app.log(tag, "moveDirection=" + moveDirection);

                BrainCore.MoveResult moveResult = BrainCore.move(world, player, moveDirection, sounds);
                acted = moveResult.acted;
                if (moveResult.moved) {
                    world.updateDijkstraMap(moveResult.endPosition.x, moveResult.endPosition.y);
                }
            }

            if (!acted) {
                acted = processActions(world);
            }
        }
        return acted;
    }

    private boolean processActions(final World world) {
        boolean acted = false;

        final Entity player = world.getEntity(world.playerEntityId);
        final MoveAnimation moveAnimation = player.getComponent(MoveAnimation.class);
        ItemContainer itemContainer = player.getComponent(ItemContainer.class);

        PlayerInputAdapter.Action action;
        while ((action = playerInputAdapter.popAction()) != null) {
            if (action == PlayerInputAdapter.Action.FIRE) {
                int ammoCount = itemContainer.getAmount(ItemType.Ammo);
                if (ammoCount > 0) {
                    Gdx.app.log(tag, "FIRE");
                    itemContainer.remove(ItemType.Ammo, 1);
                    BrainCore.fire(world, player, moveAnimation.direction, ItemType.Ammo, sounds);
                    acted = true;
                }
            } else if (action == PlayerInputAdapter.Action.ROCKET) {
                int rocketCount = itemContainer.getAmount(ItemType.Rocket);
                if (rocketCount > 0) {
                    Gdx.app.log(tag, "ROCKET");
                    itemContainer.remove(ItemType.Rocket, 1);
                    BrainCore.fire(world, player, moveAnimation.direction, ItemType.Rocket, sounds);
                    acted = true;
                }
            }
        }
        return acted;
    }

}
